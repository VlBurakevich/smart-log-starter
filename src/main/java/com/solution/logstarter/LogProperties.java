package com.solution.logstarter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "smart-logs")
public class LogProperties {
    private boolean enabled = true;
    private String apiKey;
    private String serverUrl;
    private String applicationName;
    private int scheduledDelay = 5;
    private int batchSize = 50;
}
