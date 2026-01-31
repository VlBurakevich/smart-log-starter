package com.solution.logstarter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HttpLogAppender extends AppenderBase<ILoggingEvent> {
    private final ConcurrentLinkedQueue<LogDto> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicBoolean isFlushed = new AtomicBoolean(false);

    private final LogProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private ExecutorService executor;

    public HttpLogAppender(LogProperties properties) {
        this.properties = properties;
    }

    @Override
    public void start() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "log-sender-thread");
            t.setDaemon(false);
            return t;
        });
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

        if (currentSize >= properties.getBatchSize() && !isFlushed.get()) {
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
            if (count.get() >= properties.getBatchSize() && isFlushed.compareAndSet(false, true)) {
                executor.execute(this::flush);
            }
        }
    }

    private void sendToServer(List<LogDto> logs) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Api-Key", properties.getApiKey());
            headers.set("Service-Name", properties.getApplicationName());

            HttpEntity<List<LogDto>> requestEntity = new HttpEntity<>(logs, headers);

            restTemplate.postForEntity(properties.getServerUrl(), requestEntity, String.class);

        } catch (Exception e) {
            addError("Failed to send batch: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.execute(() -> {
                List<LogDto> finalBatch = new ArrayList<>();
                LogDto item;
                while ((item = queue.poll()) != null) {
                    finalBatch.add(item);
                }
                if (!finalBatch.isEmpty()) {
                    sendToServer(finalBatch);
                }
            });

            executor.shutdown();

            try {
                if (!executor.awaitTermination(properties.getShutdownTimeoutSec(), TimeUnit.SECONDS)) {
                    addError("Logs may be lost: timeout reached");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        super.stop();
    }
}
