package com.solution.logstarter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HttpLogAppender extends AppenderBase<ILoggingEvent> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ConcurrentLinkedQueue<LogDto> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicBoolean isFlushed = new AtomicBoolean(false);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final LogProperties properties;
    private ScheduledExecutorService executor;

    public HttpLogAppender(LogProperties properties) {
        this.properties = properties;
    }

    public LogProperties getProperties() {
        return properties;
    }

    @Override
    public void start() {
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "log-sender-thread");
            t.setDaemon(false);
            return t;
        });

        long interval = properties.getScheduledDelay() > 0 ? properties.getScheduledDelay() : 5;
        executor.scheduleWithFixedDelay(this::flush, interval, interval, TimeUnit.SECONDS);

        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!properties.isEnabled()) return;

        String message = event.getFormattedMessage();

        if (event.getThrowableProxy() != null) {
            IThrowableProxy proxy = event.getThrowableProxy();
            message = Arrays.stream(proxy.getStackTraceElementProxyArray())
                    .limit(properties.getMaxStackTraceLines())
                    .map(step -> "\tat " + step.toString())
                    .collect(Collectors.joining("\n", message + "\n",
                            proxy.getStackTraceElementProxyArray().length > properties.getMaxStackTraceLines()
                                    ? "\n\t... more" : ""
                    ));
        }

        LogDto log = new LogDto(
                event.getLevel().toString(),
                message,
                event.getLoggerName(),
                event.getTimeStamp()
        );

        queue.offer(log);
        int currentSize = count.incrementAndGet();

        if (currentSize >= properties.getBatchSize()) {
            executor.execute(this::flush);
        }
    }

    private void flush() {
        if (!isFlushed.compareAndSet(false, true)) {
            return;
        }

        try {
            List<LogDto> batch = new ArrayList<>();
            LogDto item;

            while (batch.size() < properties.getBatchSize() &&(item = queue.poll()) != null) {
                batch.add(item);
                count.decrementAndGet();
            }

            if (!batch.isEmpty()) {
                sendToServer(batch);
            }
        } finally {
            isFlushed.set(false);
        }
    }

    @SuppressWarnings("java:S106")
    private void sendToServer(List<LogDto> logs) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(logs);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(properties.getServerUrl()))
                    .header("Content-Type", "application/json")
                    .header("Api-Key", properties.getApiKey())
                    .header("Service-Name", properties.getApplicationName())
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();

            if (properties.isDebugHttpTraffic()) {
                System.out.printf("[Smart-logs] Sending to: %s | App: %s | Payload: %d bytes%n",
                        request.uri(),
                        properties.getApplicationName(),
                        json.length()
                );
            }

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());

        } catch (Exception e) {
            addError("Failed to send batch: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (executor != null) {

            executor.shutdown();

            try {
                if (!executor.awaitTermination(properties.getShutdownTimeoutSec(), TimeUnit.SECONDS)) {
                    addError("Executor did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<LogDto> finalBatch = new ArrayList<>();
            LogDto item;
            while ((item = queue.poll()) != null) {
                finalBatch.add(item);
            }

            if (!finalBatch.isEmpty()) {
                sendToServer(finalBatch);
            }
        }
        super.stop();
    }
}
