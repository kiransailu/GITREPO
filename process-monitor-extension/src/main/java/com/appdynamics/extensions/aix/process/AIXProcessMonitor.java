package com.appdynamics.extensions.aix.process;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.util.AssertUtils;
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
import java.util.concurrent.TimeUnit;

public class AIXProcessMonitor extends ABaseMonitor {

    private static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|AIX Process Monitor|";
    private static final String MONITOR_NAME = "AIXProcessMonitor";
    
    private ScheduledExecutorService scheduler;
    private ObjectMapper objectMapper;

    public AIXProcessMonitor() {
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
            List<ProcessConfig> processConfigs = fetchProcessConfigurations(config);
            
            // Monitor each configured process
            for (ProcessConfig processConfig : processConfigs) {
                processProcessConfig(processConfig, tasksExecutionServiceProvider);
            }
            
        } catch (Exception e) {
            logger.error("Error during process monitoring execution", e);
        }
    }

    private List<ProcessConfig> fetchProcessConfigurations(Map<String, ?> config) throws Exception {
        List<ProcessConfig> processConfigs = new ArrayList<>();
        
        // Get GitHub configuration URL from config.yml
        String configUrl = (String) config.get("configUrl");
        String inventoryHostname = System.getProperty("inventory_hostname", "localhost");
        
        if (configUrl != null && !configUrl.isEmpty()) {
            String fullUrl = configUrl.replace("{inventory_hostname}", inventoryHostname);
            JsonNode configJson = fetchConfigurationFromUrl(fullUrl);
            
            if (configJson != null && configJson.has("process_monitor")) {
                JsonNode processMonitor = configJson.get("process_monitor");
                if (processMonitor.has("monitors")) {
                    JsonNode monitors = processMonitor.get("monitors");
                    
                    for (JsonNode monitor : monitors) {
                        ProcessConfig processConfig = new ProcessConfig();
                        processConfig.setDisplayName(monitor.get("displayname").asText());
                        processConfig.setRegex(monitor.get("regex").asText());
                        processConfig.setAssignmentGroup(monitor.get("assignment_group").asText());
                        processConfig.setHealthRules(monitor.get("health_rules").asText());
                        processConfigs.add(processConfig);
                    }
                }
            }
        }
        
        return processConfigs;
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

    private void processProcessConfig(ProcessConfig processConfig, 
                                    TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        try {
            List<ProcessInfo> processes = getAIXProcesses(processConfig.getRegex());
            
            // Report running instances count
            int runningInstances = processes.size();
            String metricPath = getDefaultMetricPrefix() + processConfig.getDisplayName() + "|Running Instances";
            tasksExecutionServiceProvider.getMetricWriteHelper().printMetric(metricPath, runningInstances);
            
            // If only one instance is running, report detailed metrics
            if (runningInstances == 1) {
                ProcessInfo process = processes.get(0);
                
                String baseMetricPath = getDefaultMetricPrefix() + processConfig.getDisplayName() + "|";
                
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "CPU%", process.getCpuPercent());
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Memory%", process.getMemoryPercent());
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "RSS", process.getRss());
            }
            
            logger.info("Processed monitor: " + processConfig.getDisplayName() + 
                       ", Running Instances: " + runningInstances);
                       
        } catch (Exception e) {
            logger.error("Error processing process config: " + processConfig.getDisplayName(), e);
        }
    }

    private List<ProcessInfo> getAIXProcesses(String regex) throws Exception {
        List<ProcessInfo> processes = new ArrayList<>();
        
        // AIX-specific ps command
        String command = "ps -eo pid,pcpu,pmem,rss,args";
        
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
            
            String[] parts = line.split("\\s+", 5);
            if (parts.length >= 5) {
                String args = parts[4];
                
                // Check if process matches regex
                if (args.matches(regex)) {
                    ProcessInfo processInfo = new ProcessInfo();
                    processInfo.setPid(Integer.parseInt(parts[0]));
                    processInfo.setCpuPercent(Double.parseDouble(parts[1]));
                    processInfo.setMemoryPercent(Double.parseDouble(parts[2]));
                    processInfo.setRss(Long.parseLong(parts[3]));
                    processInfo.setArgs(args);
                    
                    processes.add(processInfo);
                }
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            logger.warn("ps command exited with code: " + exitCode);
        }
        
        return processes;
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        Map<String, ?> config = getContextConfiguration().getConfigYml();
        return (List<Map<String, ?>>) config.get("servers");
    }

    // Process configuration class
    public static class ProcessConfig {
        private String displayName;
        private String regex;
        private String assignmentGroup;
        private String healthRules;

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getRegex() { return regex; }
        public void setRegex(String regex) { this.regex = regex; }
        
        public String getAssignmentGroup() { return assignmentGroup; }
        public void setAssignmentGroup(String assignmentGroup) { this.assignmentGroup = assignmentGroup; }
        
        public String getHealthRules() { return healthRules; }
        public void setHealthRules(String healthRules) { this.healthRules = healthRules; }
    }

    // Process information class
    public static class ProcessInfo {
        private int pid;
        private double cpuPercent;
        private double memoryPercent;
        private long rss;
        private String args;

        public int getPid() { return pid; }
        public void setPid(int pid) { this.pid = pid; }
        
        public double getCpuPercent() { return cpuPercent; }
        public void setCpuPercent(double cpuPercent) { this.cpuPercent = cpuPercent; }
        
        public double getMemoryPercent() { return memoryPercent; }
        public void setMemoryPercent(double memoryPercent) { this.memoryPercent = memoryPercent; }
        
        public long getRss() { return rss; }
        public void setRss(long rss) { this.rss = rss; }
        
        public String getArgs() { return args; }
        public void setArgs(String args) { this.args = args; }
    }
}