package com.appdynamics.extensions.aix.service;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AIXServiceMonitor extends ABaseMonitor {

    private static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|AIX Service Monitor|";
    private static final String MONITOR_NAME = "AIXServiceMonitor";
    
    private ScheduledExecutorService scheduler;
    private ObjectMapper objectMapper;

    public AIXServiceMonitor() {
        String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
        logger.info(msg);
        System.out.println(msg);
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    @Override
    protected String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        Map<String, ?> config = getContextConfiguration().getConfigYml();
        
        try {
            // Fetch centralized configuration
            List<ServiceConfig> serviceConfigs = fetchServiceConfigurations(config);
            
            // Monitor each configured service
            for (ServiceConfig serviceConfig : serviceConfigs) {
                processServiceConfig(serviceConfig, tasksExecutionServiceProvider);
            }
            
        } catch (Exception e) {
            logger.error("Error during service monitoring execution", e);
        }
    }

    private List<ServiceConfig> fetchServiceConfigurations(Map<String, ?> config) throws Exception {
        List<ServiceConfig> serviceConfigs = new ArrayList<>();
        
        // Get GitHub configuration URL from config.yml
        String configUrl = (String) config.get("configUrl");
        String inventoryHostname = System.getProperty("inventory_hostname", "localhost");
        
        if (configUrl != null && !configUrl.isEmpty()) {
            String fullUrl = configUrl.replace("{inventory_hostname}", inventoryHostname);
            JsonNode configJson = fetchConfigurationFromUrl(fullUrl);
            
            if (configJson != null && configJson.has("service_monitor")) {
                JsonNode serviceMonitor = configJson.get("service_monitor");
                if (serviceMonitor.has("service")) {
                    JsonNode services = serviceMonitor.get("service");
                    
                    for (JsonNode service : services) {
                        ServiceConfig serviceConfig = new ServiceConfig();
                        serviceConfig.setAssignmentGroup(service.get("assignment_group").asText());
                        
                        // Parse comma-separated service names
                        String serviceNames = service.get("service").asText();
                        List<String> serviceList = Arrays.asList(serviceNames.split("\\s*,\\s*"));
                        serviceConfig.setServiceNames(serviceList);
                        
                        serviceConfigs.add(serviceConfig);
                    }
                }
            }
        }
        
        return serviceConfigs;
    }

    private JsonNode fetchConfigurationFromUrl(String url) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String jsonContent = EntityUtils.toString(response.getEntity());
                    return objectMapper.readTree(jsonContent);
                } else {
                    logger.warn("Failed to fetch configuration from URL: " + url + 
                               ", Status: " + response.getStatusLine().getStatusCode());
                }
            }
        }
        return null;
    }

    private void processServiceConfig(ServiceConfig serviceConfig, 
                                    TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        try {
            for (String serviceName : serviceConfig.getServiceNames()) {
                ServiceStatus status = getAIXServiceStatus(serviceName.trim());
                
                String metricPath = getDefaultMetricPrefix() + serviceConfig.getAssignmentGroup() + 
                                   "|" + serviceName.trim() + "|Status";
                
                int statusValue = status.isActive() ? 1 : 0;
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(metricPath, statusValue);
                
                // Also report uptime if service is active
                if (status.isActive() && status.getUptime() > 0) {
                    String uptimeMetricPath = getDefaultMetricPrefix() + serviceConfig.getAssignmentGroup() + 
                                            "|" + serviceName.trim() + "|Uptime (seconds)";
                    tasksExecutionServiceProvider.getMetricWriteHelper()
                        .printMetric(uptimeMetricPath, status.getUptime());
                }
                
                logger.info("Processed service: " + serviceName + ", Status: " + 
                           (status.isActive() ? "Active" : "Inactive") + 
                           ", Group: " + serviceConfig.getAssignmentGroup());
            }
                       
        } catch (Exception e) {
            logger.error("Error processing service config: " + serviceConfig.getAssignmentGroup(), e);
        }
    }

    private ServiceStatus getAIXServiceStatus(String serviceName) throws Exception {
        ServiceStatus status = new ServiceStatus();
        status.setServiceName(serviceName);
        status.setActive(false);
        status.setUptime(0);
        
        // First try lssrc command (AIX System Resource Controller)
        try {
            String command = "lssrc -s " + serviceName;
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            boolean headerSkipped = false;
            
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Skip header line
                }
                
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // AIX lssrc output format: Subsystem Group PID Status
                String[] parts = line.split("\\s+");
                if (parts.length >= 4) {
                    String subsystemStatus = parts[3].toLowerCase();
                    status.setActive("active".equals(subsystemStatus));
                    
                    // If PID is available, service is running
                    if (parts.length > 2 && !parts[2].equals("")) {
                        try {
                            int pid = Integer.parseInt(parts[2]);
                            if (pid > 0) {
                                status.setActive(true);
                                // Get process uptime
                                status.setUptime(getProcessUptime(pid));
                            }
                        } catch (NumberFormatException e) {
                            // PID not available or invalid
                        }
                    }
                    break;
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return status; // Successfully found service via lssrc
            }
            
        } catch (Exception e) {
            logger.debug("lssrc failed for service: " + serviceName + ", trying ps command");
        }
        
        // If lssrc fails, try process-based detection using ps
        try {
            String command = "ps -ef | grep " + serviceName + " | grep -v grep";
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    status.setActive(true);
                    // Try to extract PID for uptime calculation
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        try {
                            int pid = Integer.parseInt(parts[1]);
                            status.setUptime(getProcessUptime(pid));
                        } catch (NumberFormatException e) {
                            // Could not get PID
                        }
                    }
                    break;
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to check service status for: " + serviceName, e);
        }
        
        return status;
    }

    private long getProcessUptime(int pid) {
        try {
            // AIX ps command to get process start time
            String command = "ps -o etime= -p " + pid;
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String etime = reader.readLine();
            if (etime != null && !etime.trim().isEmpty()) {
                return parseElapsedTime(etime.trim());
            }
            
        } catch (Exception e) {
            logger.debug("Failed to get uptime for PID: " + pid, e);
        }
        return 0;
    }

    private long parseElapsedTime(String etime) {
        try {
            // Parse AIX etime format (can be: seconds, MM:SS, HH:MM:SS, or DD-HH:MM:SS)
            long totalSeconds = 0;
            
            if (etime.contains("-")) {
                // Format: DD-HH:MM:SS
                String[] daysPart = etime.split("-");
                int days = Integer.parseInt(daysPart[0]);
                String timePart = daysPart[1];
                totalSeconds += days * 24 * 3600;
                etime = timePart;
            }
            
            String[] timeParts = etime.split(":");
            if (timeParts.length == 3) {
                // Format: HH:MM:SS
                totalSeconds += Integer.parseInt(timeParts[0]) * 3600; // hours
                totalSeconds += Integer.parseInt(timeParts[1]) * 60;   // minutes
                totalSeconds += Integer.parseInt(timeParts[2]);        // seconds
            } else if (timeParts.length == 2) {
                // Format: MM:SS
                totalSeconds += Integer.parseInt(timeParts[0]) * 60;   // minutes
                totalSeconds += Integer.parseInt(timeParts[1]);        // seconds
            } else if (timeParts.length == 1) {
                // Format: SS (just seconds)
                totalSeconds = Integer.parseInt(timeParts[0]);
            }
            
            return totalSeconds;
            
        } catch (Exception e) {
            logger.debug("Failed to parse elapsed time: " + etime, e);
            return 0;
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        Map<String, ?> config = getContextConfiguration().getConfigYml();
        return (List<Map<String, ?>>) config.get("servers");
    }

    // Service configuration class
    public static class ServiceConfig {
        private String assignmentGroup;
        private List<String> serviceNames;

        public String getAssignmentGroup() { return assignmentGroup; }
        public void setAssignmentGroup(String assignmentGroup) { this.assignmentGroup = assignmentGroup; }
        
        public List<String> getServiceNames() { return serviceNames; }
        public void setServiceNames(List<String> serviceNames) { this.serviceNames = serviceNames; }
    }

    // Service status class
    public static class ServiceStatus {
        private String serviceName;
        private boolean active;
        private long uptime;

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public long getUptime() { return uptime; }
        public void setUptime(long uptime) { this.uptime = uptime; }
    }
}