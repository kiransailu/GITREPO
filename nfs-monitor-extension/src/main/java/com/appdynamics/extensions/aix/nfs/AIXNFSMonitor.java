package com.appdynamics.extensions.aix.nfs;

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

public class AIXNFSMonitor extends ABaseMonitor {

    private static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|AIX NFS Monitor|";
    private static final String MONITOR_NAME = "AIXNFSMonitor";
    
    private ScheduledExecutorService scheduler;
    private ObjectMapper objectMapper;

    public AIXNFSMonitor() {
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
            List<NFSConfig> nfsConfigs = fetchNFSConfigurations(config);
            
            // Monitor each configured NFS mount
            for (NFSConfig nfsConfig : nfsConfigs) {
                processNFSConfig(nfsConfig, tasksExecutionServiceProvider);
            }
            
        } catch (Exception e) {
            logger.error("Error during NFS monitoring execution", e);
        }
    }

    private List<NFSConfig> fetchNFSConfigurations(Map<String, ?> config) throws Exception {
        List<NFSConfig> nfsConfigs = new ArrayList<>();
        
        // Get GitHub configuration URL from config.yml
        String configUrl = (String) config.get("configUrl");
        String inventoryHostname = System.getProperty("inventory_hostname", "localhost");
        
        if (configUrl != null && !configUrl.isEmpty()) {
            String fullUrl = configUrl.replace("{inventory_hostname}", inventoryHostname);
            JsonNode configJson = fetchConfigurationFromUrl(fullUrl);
            
            if (configJson != null && configJson.has("nfs_monitor")) {
                JsonNode nfsMonitor = configJson.get("nfs_monitor");
                if (nfsMonitor.has("NFS")) {
                    JsonNode nfsMounts = nfsMonitor.get("NFS");
                    
                    for (JsonNode mount : nfsMounts) {
                        NFSConfig nfsConfig = new NFSConfig();
                        nfsConfig.setNfsMountPath(mount.get("nfsMountsToMonitor").asText().replace("\"", ""));
                        nfsConfig.setDisplayName(mount.get("displayname").asText());
                        nfsConfig.setAssignmentGroup(mount.get("assignment_group").asText());
                        nfsConfig.setHealthRules(mount.get("health_rules").asText());
                        nfsConfigs.add(nfsConfig);
                    }
                }
            }
        }
        
        return nfsConfigs;
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

    private void processNFSConfig(NFSConfig nfsConfig, 
                                 TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        try {
            NFSStats nfsStats = getAIXNFSStats(nfsConfig.getNfsMountPath());
            
            if (nfsStats != null) {
                String baseMetricPath = getDefaultMetricPrefix() + nfsConfig.getDisplayName() + "|";
                
                // Report all NFS metrics
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Total Space (KB)", nfsStats.getTotalSpace());
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Free Space (KB)", nfsStats.getFreeSpace());
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Used Space (KB)", nfsStats.getUsedSpace());
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Usage Percentage", nfsStats.getUsagePercentage());
                    
                logger.info("Processed NFS monitor: " + nfsConfig.getDisplayName() + 
                           ", Usage: " + nfsStats.getUsagePercentage() + "%");
            } else {
                // Report unavailable NFS mount
                String baseMetricPath = getDefaultMetricPrefix() + nfsConfig.getDisplayName() + "|";
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Available", 0);
                    
                logger.warn("NFS mount not available: " + nfsConfig.getNfsMountPath());
            }
                       
        } catch (Exception e) {
            logger.error("Error processing NFS config: " + nfsConfig.getDisplayName(), e);
        }
    }

    private NFSStats getAIXNFSStats(String mountPath) throws Exception {
        // AIX-specific df command
        String command = "df -k " + mountPath;
        
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        String line;
        boolean headerSkipped = false;
        NFSStats stats = null;
        
        while ((line = reader.readLine()) != null) {
            if (!headerSkipped) {
                headerSkipped = true;
                continue; // Skip header line
            }
            
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // AIX df output format: Filesystem 1024-blocks Used Available Use% Mounted on
            String[] parts = line.split("\\s+");
            if (parts.length >= 6) {
                try {
                    long totalSpace = Long.parseLong(parts[1]); // 1024-blocks
                    long usedSpace = Long.parseLong(parts[2]); // Used
                    long freeSpace = Long.parseLong(parts[3]); // Available
                    
                    // Calculate usage percentage
                    double usagePercentage = (double) usedSpace / totalSpace * 100.0;
                    
                    stats = new NFSStats();
                    stats.setTotalSpace(totalSpace);
                    stats.setUsedSpace(usedSpace);
                    stats.setFreeSpace(freeSpace);
                    stats.setUsagePercentage(usagePercentage);
                    
                    break; // We found our filesystem info
                    
                } catch (NumberFormatException e) {
                    logger.warn("Failed to parse df output: " + line);
                }
            }
        }
        
        // Check for errors
        StringBuilder errorOutput = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            logger.warn("df command failed for " + mountPath + " with exit code: " + exitCode + 
                       ", Error: " + errorOutput.toString());
        }
        
        return stats;
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        Map<String, ?> config = getContextConfiguration().getConfigYml();
        return (List<Map<String, ?>>) config.get("servers");
    }

    // NFS configuration class
    public static class NFSConfig {
        private String nfsMountPath;
        private String displayName;
        private String assignmentGroup;
        private String healthRules;

        public String getNfsMountPath() { return nfsMountPath; }
        public void setNfsMountPath(String nfsMountPath) { this.nfsMountPath = nfsMountPath; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getAssignmentGroup() { return assignmentGroup; }
        public void setAssignmentGroup(String assignmentGroup) { this.assignmentGroup = assignmentGroup; }
        
        public String getHealthRules() { return healthRules; }
        public void setHealthRules(String healthRules) { this.healthRules = healthRules; }
    }

    // NFS statistics class
    public static class NFSStats {
        private long totalSpace;
        private long usedSpace;
        private long freeSpace;
        private double usagePercentage;

        public long getTotalSpace() { return totalSpace; }
        public void setTotalSpace(long totalSpace) { this.totalSpace = totalSpace; }
        
        public long getUsedSpace() { return usedSpace; }
        public void setUsedSpace(long usedSpace) { this.usedSpace = usedSpace; }
        
        public long getFreeSpace() { return freeSpace; }
        public void setFreeSpace(long freeSpace) { this.freeSpace = freeSpace; }
        
        public double getUsagePercentage() { return usagePercentage; }
        public void setUsagePercentage(double usagePercentage) { this.usagePercentage = usagePercentage; }
    }
}