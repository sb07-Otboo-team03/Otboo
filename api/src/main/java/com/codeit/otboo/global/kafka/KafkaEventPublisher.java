package com.codeit.otboo.global.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.Nullable;
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
        String message = serialize(topic, event, key);

        if (key == null) {
            kafkaTemplate.send(topic, message)
                    .whenComplete((result, ex) ->
                            handleResult(topic, null, event, ex));
            return;
        }
        kafkaTemplate.send(topic, key, message);
    }

    private String serialize(String topic, Object event, @Nullable String key) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error(
                    "카프카 역직렬화 실패. topic={}, key={}, eventType={}",
                    topic,
                    key,
                    event.getClass().getSimpleName(),
                    e
            );
            throw new RuntimeException(e);
        }
    }

    private void handleResult(String topic, @Nullable String key, Object event, Throwable ex) {
        if (ex != null) {
            log.error(
                    "카프카 발행 실패. topic={}, key={}, eventType={}",
                    topic,
                    key,
                    event.getClass().getSimpleName(),
                    ex
            );
            return;
        }

        log.debug(
                "카프카 발행 성공. topic={}, key={}, eventType={}",
                topic,
                key,
                event.getClass().getSimpleName()
        );
    }
}
