package com.solution.logstarter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpLogAppenderTest {
    private static final WireMockServer wireMock = new WireMockServer(options().dynamicPort());
    private HttpLogAppender appender;
    private LogProperties properties;

    @BeforeAll
    static void startWireMock() {
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();

        properties = new LogProperties();
        properties.setEnabled(true);
        properties.setServerUrl(wireMock.baseUrl() + "/logs");
        properties.setApiKey("test-key-123");
        properties.setApplicationName("test-service");
        properties.setBatchSize(2);
        properties.setMaxStackTraceLines(3);
        properties.setShutdownTimeoutSec(1);

        appender = new HttpLogAppender(properties);
        appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        appender.start();
    }

    @AfterEach
    void tearDown() {
        appender.stop();
    }

    @Test
    void shouldSendBatchWhenFull() {
        wireMock.stubFor(post("/logs").willReturn(ok()));

        appender.append(createMockEvent("Message 1", Level.INFO));

        wireMock.verify(0, postRequestedFor(urlEqualTo("/logs")));

        appender.append(createMockEvent("Message 2", Level.ERROR));

        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> wireMock.verify(1, postRequestedFor(urlEqualTo("/logs"))
                        .withHeader("X-Api-Key", equalTo("test-key-123"))
                        .withHeader("Service-Name", equalTo("test-service"))
                        .withRequestBody(containing("Message 1"))
                        .withRequestBody(containing("Message 2"))
                ));
    }

    @Test
    void shouldFormatAndLimitStackTrace() {
        wireMock.stubFor(post("/logs").willReturn(ok()));
        properties.setBatchSize(1);

        ILoggingEvent event = createMockEvent("Error with stack", Level.ERROR);
        IThrowableProxy throwableProxy = mock(IThrowableProxy.class);

        StackTraceElementProxy step = mock(StackTraceElementProxy.class);
        when(step.toString()).thenReturn("com.test.Class.method(Class.java:10)");
        StackTraceElementProxy[] steps = new StackTraceElementProxy[]{step, step, step, step, step};

        when(event.getThrowableProxy()).thenReturn(throwableProxy);
        when(throwableProxy.getStackTraceElementProxyArray()).thenReturn(steps);

        appender.append(event);

        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> wireMock.verify(1, postRequestedFor(urlEqualTo("/logs"))
                        .withRequestBody(containing("... more"))
                ));
    }

    @Test
    void shouldFlushRemainingLogsOnStop() {
        wireMock.stubFor(post("/logs").willReturn(ok()));

        appender.append(createMockEvent("Last wish before death", Level.WARN));

        appender.stop();

        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> wireMock.verify(postRequestedFor(urlEqualTo("/logs"))
                        .withRequestBody(containing("Last wish before death"))
                ));
    }

    @Test
    void shouldDoNothingIfDisabled() {
        properties.setEnabled(false);
        appender.append(createMockEvent("Silent log", Level.INFO));

        wireMock.verify(0, postRequestedFor(urlEqualTo("/logs")));
    }

    @Test
    void shouldHandleServerErrorGracefully() {
        wireMock.stubFor(post("/logs").willReturn(serverError()));
        properties.setBatchSize(1);

        Assertions.assertDoesNotThrow(() -> appender.append(createMockEvent("Resilient log", Level.INFO)));

        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> wireMock.verify(1, postRequestedFor(urlEqualTo("/logs"))));

        assertThat(appender.isStarted()).isTrue();
    }

    private ILoggingEvent createMockEvent(String message, Level level) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(message);
        when(event.getLevel()).thenReturn(level);
        when(event.getLoggerName()).thenReturn("test-logger");
        when(event.getTimeStamp()).thenReturn(System.currentTimeMillis());

        return event;
    }
}
