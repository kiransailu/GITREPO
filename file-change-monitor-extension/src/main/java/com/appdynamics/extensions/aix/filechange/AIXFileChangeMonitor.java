package com.appdynamics.extensions.aix.filechange;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AIXFileChangeMonitor extends ABaseMonitor {

    private static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|AIX File Change Monitor|";
    private static final String MONITOR_NAME = "AIXFileChangeMonitor";
    
    private ScheduledExecutorService scheduler;
    private ObjectMapper objectMapper;
    private Map<String, FileState> fileStates = new HashMap<>();

    public AIXFileChangeMonitor() {
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
            List<FileConfig> fileConfigs = fetchFileConfigurations(config);
            
            // Monitor each configured file
            for (FileConfig fileConfig : fileConfigs) {
                processFileConfig(fileConfig, tasksExecutionServiceProvider);
            }
            
        } catch (Exception e) {
            logger.error("Error during file change monitoring execution", e);
        }
    }

    private List<FileConfig> fetchFileConfigurations(Map<String, ?> config) throws Exception {
        List<FileConfig> fileConfigs = new ArrayList<>();
        
        // Get GitHub configuration URL from config.yml
        String configUrl = (String) config.get("configUrl");
        String inventoryHostname = System.getProperty("inventory_hostname", "localhost");
        
        if (configUrl != null && !configUrl.isEmpty()) {
            String fullUrl = configUrl.replace("{inventory_hostname}", inventoryHostname);
            JsonNode configJson = fetchConfigurationFromUrl(fullUrl);
            
            if (configJson != null && configJson.has("monitored_files")) {
                JsonNode monitoredFiles = configJson.get("monitored_files");
                
                for (JsonNode file : monitoredFiles) {
                    FileConfig fileConfig = new FileConfig();
                    fileConfig.setFilePath(file.get("name").asText());
                    fileConfig.setLastModifiedCheckInterval(file.get("last_modified_check").asInt());
                    fileConfigs.add(fileConfig);
                }
            }
        }
        
        return fileConfigs;
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

    private void processFileConfig(FileConfig fileConfig, 
                                 TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        try {
            File file = new File(fileConfig.getFilePath());
            String fileName = file.getName();
            String baseMetricPath = getDefaultMetricPrefix() + fileName + "|";
            
            if (file.exists()) {
                long currentModTime = file.lastModified();
                long currentSize = file.length();
                long currentTime = System.currentTimeMillis();
                
                // Get previous state
                FileState previousState = fileStates.get(fileConfig.getFilePath());
                
                // Report basic file metrics
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Exists", 1);
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Size (Bytes)", currentSize);
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Last Modified Time", currentModTime);
                
                // Check if file was modified recently
                long timeSinceModified = (currentTime - currentModTime) / 1000; // Convert to seconds
                int recentlyModified = (timeSinceModified <= fileConfig.getLastModifiedCheckInterval()) ? 1 : 0;
                
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Recently Modified", recentlyModified);
                
                // Calculate change metrics if we have previous state
                if (previousState != null) {
                    // Check if file was modified since last check
                    int modifiedSinceLastCheck = (currentModTime > previousState.getLastModified()) ? 1 : 0;
                    tasksExecutionServiceProvider.getMetricWriteHelper()
                        .printMetric(baseMetricPath + "Modified Since Last Check", modifiedSinceLastCheck);
                    
                    // Size change
                    long sizeChange = currentSize - previousState.getSize();
                    tasksExecutionServiceProvider.getMetricWriteHelper()
                        .printMetric(baseMetricPath + "Size Change (Bytes)", sizeChange);
                    
                    // Time since last modification (in seconds)
                    tasksExecutionServiceProvider.getMetricWriteHelper()
                        .printMetric(baseMetricPath + "Seconds Since Last Modified", timeSinceModified);
                }
                
                // Update file state
                FileState currentState = new FileState();
                currentState.setLastModified(currentModTime);
                currentState.setSize(currentSize);
                currentState.setLastChecked(currentTime);
                fileStates.put(fileConfig.getFilePath(), currentState);
                
                logger.info("Processed file: " + fileConfig.getFilePath() + 
                           ", Size: " + currentSize + " bytes, Recently Modified: " + 
                           (recentlyModified == 1 ? "Yes" : "No"));
                           
            } else {
                // File doesn't exist
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Exists", 0);
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Size (Bytes)", 0);
                tasksExecutionServiceProvider.getMetricWriteHelper()
                    .printMetric(baseMetricPath + "Recently Modified", 0);
                
                logger.warn("File not found: " + fileConfig.getFilePath());
                
                // Remove from file states if it was being tracked
                fileStates.remove(fileConfig.getFilePath());
            }
                       
        } catch (Exception e) {
            logger.error("Error processing file config: " + fileConfig.getFilePath(), e);
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        Map<String, ?> config = getContextConfiguration().getConfigYml();
        return (List<Map<String, ?>>) config.get("servers");
    }

    // File configuration class
    public static class FileConfig {
        private String filePath;
        private int lastModifiedCheckInterval; // in seconds

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public int getLastModifiedCheckInterval() { return lastModifiedCheckInterval; }
        public void setLastModifiedCheckInterval(int lastModifiedCheckInterval) { 
            this.lastModifiedCheckInterval = lastModifiedCheckInterval; 
        }
    }

    // File state tracking class
    public static class FileState {
        private long lastModified;
        private long size;
        private long lastChecked;

        public long getLastModified() { return lastModified; }
        public void setLastModified(long lastModified) { this.lastModified = lastModified; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        
        public long getLastChecked() { return lastChecked; }
        public void setLastChecked(long lastChecked) { this.lastChecked = lastChecked; }
    }
}