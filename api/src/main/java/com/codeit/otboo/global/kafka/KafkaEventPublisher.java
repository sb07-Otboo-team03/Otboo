package com.codeit.otboo.global.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String topic, Object event) {
        publish(topic, event, null);
    }

    public void publish(String topic, Object event, String key) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, message);
        } catch (Exception e) {
            log.error("Failed to send event to Kafka. topic={}, key={}", topic, key, e);
            throw new RuntimeException(e);
        }
    }
}
