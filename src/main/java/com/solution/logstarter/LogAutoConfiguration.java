package com.solution.logstarter;

import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(LogProperties.class)
public class LogAutoConfiguration {

    private final LogProperties props;

    @PostConstruct
    public void init() {
        if (!props.isEnabled()) return;

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        HttpLogAppender appender = new HttpLogAppender(props);
        appender.setContext(context);
        appender.start();

        context.getLogger("ROOT").addAppender(appender);
    }
}
