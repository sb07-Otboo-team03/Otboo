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

    @KafkaListener(topics = KafkaTopics.REALTIME_NOTIFICATION, groupId = "sse-${random.uuid}")
    public void onNotificationSseKafkaEvent(String kafkaEvent) {
        try {
            NotificationSseKafkaEvent event =
                    objectMapper.readValue(kafkaEvent, NotificationSseKafkaEvent.class);

            sendSseEvent(event.notification());
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = KafkaTopics.REALTIME_NOTIFICATION_BATCH, groupId = "sse-${random.uuid}")
    public void onNotificationBatchSseKafkaEvent(String kafkaEvent) {
        try {
            NotificationBatchSseKafkaEvent event =
                    objectMapper.readValue(kafkaEvent, NotificationBatchSseKafkaEvent.class);

            event.notifications().forEach(this::sendSseEvent);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
