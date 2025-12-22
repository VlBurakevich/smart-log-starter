package com.solution.logstarter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "smart-logs")
public class LogProperties {
    private boolean enabled = true;
    private String accountId;
    private String serverUrl;
    private String applicationName;
    private int batchSize = 10;
}
