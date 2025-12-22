package com.solution.logstarter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogDto {
    private String accountId;
    private String level;
    private String message;
    private String loggerName;
    private long timestamp;
}
