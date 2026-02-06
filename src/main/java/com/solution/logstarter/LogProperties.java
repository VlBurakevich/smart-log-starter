package com.solution.logstarter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smart-logs")
public class LogProperties {
    private boolean enabled = true;
    private boolean debugHttpTraffic = false;
    private String apiKey;
    private String serverUrl;
    private String applicationName;
    private String minLevel = "INFO";
    private int scheduledDelay = 5;
    private int batchSize = 50;
    private int shutdownTimeoutSec = 20;
    private int maxStackTraceLines = 5;


    public LogProperties() {
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

    public int getMaxStackTraceLines() {
        return maxStackTraceLines;
    }

    public void setMaxStackTraceLines(int maxStackTraceLines) {
        this.maxStackTraceLines = maxStackTraceLines;
    }

    public boolean isDebugHttpTraffic() {
        return debugHttpTraffic;
    }

    public void setDebugHttpTraffic(boolean debugHttpTraffic) {
        this.debugHttpTraffic = debugHttpTraffic;
    }

    public String getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(String minLevel) {
        this.minLevel = minLevel;
    }
}
