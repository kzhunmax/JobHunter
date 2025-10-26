package com.github.kzhunmax.jobsearch.event.producer;

import com.github.kzhunmax.jobsearch.shared.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserEvent(UserEvent event) {
        kafkaTemplate.send("user-events", event.email(), event);
        log.info("Sent UserEvent to Kafka {}", event);
    }
}
