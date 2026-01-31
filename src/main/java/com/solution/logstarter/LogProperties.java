package com.solution.logstarter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smart-logs")
public class LogProperties {
    private boolean enabled = true;
    private String apiKey;
    private String serverUrl;
    private String applicationName;
    private int scheduledDelay = 5;
    private int batchSize = 50;
    private int shutdownTimeoutSec = 20;

    public LogProperties() {
    }

    public LogProperties(
            boolean enabled, String apiKey, String serverUrl, String applicationName,
            int scheduledDelay, int batchSize, int shutdownTimeoutSec
    ) {
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.serverUrl = serverUrl;
        this.applicationName = applicationName;
        this.scheduledDelay = scheduledDelay;
        this.batchSize = batchSize;
        this.shutdownTimeoutSec = shutdownTimeoutSec;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getScheduledDelay() {
        return scheduledDelay;
    }

    public void setScheduledDelay(int scheduledDelay) {
        this.scheduledDelay = scheduledDelay;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getShutdownTimeoutSec() {
        return shutdownTimeoutSec;
    }

    public void setShutdownTimeoutSec(int shutdownTimeoutSec) {
        this.shutdownTimeoutSec = shutdownTimeoutSec;
    }
}
