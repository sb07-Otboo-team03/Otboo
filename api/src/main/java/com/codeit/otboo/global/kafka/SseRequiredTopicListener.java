package com.codeit.otboo.global.kafka;

import com.codeit.otboo.global.kafka.event.NotificationBatchSseKafkaEvent;
import com.codeit.otboo.global.kafka.event.NotificationSseKafkaEvent;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.sse.service.SseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseRequiredTopicListener {

    private final SseService sseService;
    private final ObjectMapper objectMapper;

    private void sendSseEvent(NotificationDto notificationDto) {
        sseService.send(
                Set.of(notificationDto.receiverId()),
                "notifications",
                notificationDto
        );
    }

    @KafkaListener(
            topics = KafkaTopics.REALTIME_NOTIFICATION_SINGLE,
            groupId = "otboo-sse-${app.instance-id}"
    )
    public void onNotificationSseKafkaEvent(String kafkaEvent) {
        NotificationSseKafkaEvent event = deserialize(
                kafkaEvent,
                NotificationSseKafkaEvent.class
        );

        if (event == null) return;

        sendSseEvent(event.notification());
    }

    @KafkaListener(
            topics = KafkaTopics.REALTIME_NOTIFICATION_BATCH,
            groupId = "otboo-sse-${app.instance-id}"
    )
    public void onNotificationBatchSseKafkaEvent(String kafkaEvent) {
        NotificationBatchSseKafkaEvent event = deserialize(
                kafkaEvent,
                NotificationBatchSseKafkaEvent.class
        );

        if (event == null) return;

        event.notifications().forEach(this::sendSseEvent);
    }

    private <T> T deserialize(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException e) {
            log.error(
                    "카프카 역직렬화 실패. targetType={}, payload={}",
                    type.getSimpleName(),
                    payload,
                    e
            );
            return null;
        }
    }
}
