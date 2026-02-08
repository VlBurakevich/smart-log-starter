package com.solution.logstarter;

import java.time.OffsetDateTime;

public class LogDto {
    private String level;
    private String message;
    private String loggerName;
    private OffsetDateTime timestamp;

    public LogDto() {
    }

    public LogDto(String level, String message, String loggerName, OffsetDateTime timestamp) {
        this.level = level;
        this.message = message;
        this.loggerName = loggerName;
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
