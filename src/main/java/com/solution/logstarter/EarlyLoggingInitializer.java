package com.solution.logstarter;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class EarlyLoggingInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    @SuppressWarnings("java:S106")
    public void initialize(ConfigurableApplicationContext context) {
        System.out.println("[Smart-logs] EarlyLoggingInitializer: initialize");

        ConfigurableEnvironment environment = context.getEnvironment();

        LogProperties properties = Binder.get(environment)
                .bind("smart-logs", LogProperties.class)
                .orElseGet(LogProperties::new);

        if (!properties.isEnabled()) {
            return;
        }

        if (prepareAndValidate(properties, environment)) {
            setupAppender(properties);
        }

        System.out.println("\n[Smart-logs] Started successfully. Application: " + properties.getApplicationName() + "\n");
    }

    @SuppressWarnings("java:S106")
    private boolean prepareAndValidate(LogProperties properties, ConfigurableEnvironment environment) {
        if (properties.getApplicationName() == null || properties.getApplicationName().isBlank()) {
            properties.setApplicationName(
                    environment.getProperty("spring.application.name", "undefined-service")
            );
        }

        if (properties.getServerUrl() == null || properties.getServerUrl().isBlank()) {
            System.err.println("\n[Smart-Logs] ERROR: 'server-url' is missing. Remote logging DISABLED.\n");
            return false;
        }

        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            System.err.println("\n[Smart-Logs] ERROR: 'api-key' is missing. Remote logging DISABLED.\n");
            return false;
        }

        return true;
    }

    private void setupAppender(LogProperties properties) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        HttpLogAppender appender = new HttpLogAppender(properties);
        appender.setContext(loggerContext);
        appender.start();

        loggerContext.getLogger("ROOT").addAppender(appender);
    }
}
