package com.solution.logstarter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogDto {
    private String level;
    private String message;
    private String loggerName;
    private long timestamp;
}
