package com.solution.logstarter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class EarlyLoggingInitializerTest {
    private final EarlyLoggingInitializer initializer = new EarlyLoggingInitializer();
    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @BeforeEach
    void setUp() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("ROOT");

        rootLogger.detachAndStopAllAppenders();
    }

    @Test
    void shouldAttachAppenderWhenEnabled() {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "smart-logs.enabled=true",
                "smart-logs.server-url=http://localhost:8080",
                "smart-logs.api-key=test-key"
        );

        initializer.initialize(context);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("ROOT");

        boolean found = false;
        Iterator<Appender<ILoggingEvent>> iterator = rootLogger.iteratorForAppenders();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof HttpLogAppender) {
                found = true;
                break;
            }
        }

        assertThat(found).isTrue();
    }

    @Test
    void shouldNotAttachAppenderWhenDisabled() {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "smart-logs.enabled=false",
                "smart-logs.server-url=http://localhost:8080",
                "smart-logs.api-key=test-key"
        );

        initializer.initialize(context);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("ROOT");

        boolean found = false;
        Iterator<Appender<ILoggingEvent>> iterator = rootLogger.iteratorForAppenders();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof HttpLogAppender) {
                found = true;
                break;
            }
        }

        assertThat(found).isFalse();
    }

    @Test
    void shouldFallbackToSpringApplicationName() {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "smart-logs.server-url=http://localhost:8080",
                "smart-logs.api-key=test-key",
                "spring.application.name=spring-application-name"
        );

        initializer.initialize(context);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        HttpLogAppender appender = (HttpLogAppender) loggerContext.getLogger("ROOT").iteratorForAppenders().next();

        assertThat(appender.getProperties().getApplicationName()).isEqualTo("spring-application-name");
    }

    @Test
    void shouldNotAttachAppenderWhenServerUrlIsMissing() {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "smart-logs.enabled=true",
                "smart-logs.api-key=test-key"
        );

        initializer.initialize(context);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        long count = StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                        loggerContext.getLogger("ROOT").iteratorForAppenders(), 0), false)
                .filter(HttpLogAppender.class::isInstance)
                .count();

        assertThat(count).isZero();
    }

    @Test
    void shouldPropagateBatchSettingsToAppender() {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "smart-logs.server-url=http://localhost:8080",
                "smart-logs.api-key=test-key",
                "smart-logs.batch-size=100",
                "smart-logs.scheduled-delay=10"
        );

        initializer.initialize(context);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        HttpLogAppender appender = (HttpLogAppender) loggerContext.getLogger("ROOT")
                .iteratorForAppenders().next();

        assertThat(appender.getProperties().getBatchSize()).isEqualTo(100);
        assertThat(appender.getProperties().getScheduledDelay()).isEqualTo(10);
    }
}
