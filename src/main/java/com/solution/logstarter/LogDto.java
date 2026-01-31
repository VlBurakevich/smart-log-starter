package com.solution.logstarter;

public class LogDto {
    private String level;
    private String message;
    private String loggerName;
    private long timestamp;

    public LogDto() {
    }

    public LogDto(String level, String message, String loggerName, long timestamp) {
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
