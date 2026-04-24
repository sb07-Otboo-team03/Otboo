package com.codeit.otboo.global.kafka;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.service.NotificationEventService;
import com.codeit.otboo.domain.sse.event.*;
import com.codeit.otboo.global.kafka.event.NotificationSseKafkaEvent;
import com.codeit.otboo.global.websocket.event.DirectMessageCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationEventService notificationEventService;

    /**
     * WebSocket 실시간 채팅 전달용 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DirectMessageCreatedEvent event) {
        DirectMessageResponse directMessageResponse = event.getData();
        String directMessageKey = DirectMessageKeyGenerator.generate(directMessageResponse);

        sendToKafka(
                KafkaTopics.REALTIME_DIRECT_MESSAGE,
                event,
                directMessageKey
        );
    }

    /**
     * 단건 알림 생성 + Kafka 발행
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DirectMessageSseEvent event) {
        NotificationDto dto = notificationEventService.createSingleNotification(
                event.getUserId(),
                event.getTitle(),
                event.getContent()
        );

        sendToKafka(
                KafkaTopics.REALTIME_NOTIFICATION,
                new NotificationSseKafkaEvent(dto),
                null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FollowSseEvent event) {
        NotificationDto dto = notificationEventService.createSingleNotification(
                event.getUserId(),
                event.getTitle(),
                event.getContent()
        );

        sendToKafka(
                KafkaTopics.REALTIME_NOTIFICATION,
                new NotificationSseKafkaEvent(dto),
                null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CommentCreatedEvent event) {
        NotificationDto dto = notificationEventService.createSingleNotification(
                event.getReceiverId(),
                event.getTitle(),
                event.getContent()
        );

        sendToKafka(
                KafkaTopics.REALTIME_NOTIFICATION,
                new NotificationSseKafkaEvent(dto),
                null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FeedLikedEvent event) {
        NotificationDto dto = notificationEventService.createSingleNotification(
                event.getReceiverId(),
                event.getTitle(),
                event.getContent()
        );

        sendToKafka(
                KafkaTopics.REALTIME_NOTIFICATION,
                new NotificationSseKafkaEvent(dto),
                null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserRoleUpdatedEvent event) {
        NotificationDto dto = notificationEventService.createSingleNotification(
                event.getReceiverId(),
                event.getTitle(),
                event.getContent()
        );

        sendToKafka(
                KafkaTopics.REALTIME_NOTIFICATION,
                new NotificationSseKafkaEvent(dto),
                null
        );
    }

    /**
     * 임시 Kafka publish 메서드
     * 다음 단계에서 KafkaEventPublisher로 분리 예정
     */
    private void sendToKafka(String topic, Object event, String key) {
        try {
            String message = objectMapper.writeValueAsString(event);

            if (key == null) {
                kafkaTemplate.send(topic, message);
                return;
            }

            kafkaTemplate.send(topic, key, message);
        } catch (JsonProcessingException e) {
            log.error("Failed to send event to Kafka. topic={}, key={}", topic, key, e);
            throw new RuntimeException(e);
        }
    }
}
