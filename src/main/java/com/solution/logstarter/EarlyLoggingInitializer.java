package com.solution.logstarter;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class EarlyLoggingInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();

        LogProperties properties = Binder.get(environment)
                .bind("smart-logs", LogProperties.class)
                .orElseGet(LogProperties::new);

        if (!properties.isEnabled()) {
            return;
        }

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        HttpLogAppender appender = new HttpLogAppender(properties);
        appender.setContext(loggerContext);
        appender.start();

        loggerContext.getLogger("ROOT").addAppender(appender);
    }
}
