package com.solution.logstarter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class HttpLogAppender extends AppenderBase<ILoggingEvent> {
    private final List<LogDto> buffer = new ArrayList<>();                 //cuncurrent collection
    private final LogProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    public HttpLogAppender(LogProperties props) {
        this.props = props;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!props.isEnabled()) return;

        LogDto log = new LogDto(
                props.getAccountId(),
                event.getLevel().toString(),
                event.getFormattedMessage(),
                event.getLoggerName(),
                event.getTimeStamp()
        );

        synchronized (buffer) {
            buffer.add(log);
            if (buffer.size() > props.getBatchSize()) {
                flush();
            }
        }
    }

    private void flush() {
        if (buffer.isEmpty()) return;
        List<LogDto> toSend = new ArrayList<>(buffer);                // мб не каждый раз создавать новый лист будет бить по GC
        buffer.clear();

        Thread.ofVirtual().start(() -> {
            try {
                restTemplate.postForEntity(props.getServerUrl(), toSend, String.class);
            } catch (Exception e) {
                addError("Smart-Logs: failed to send batch: " + e.getMessage());
            }
        });
    }
}
