package com.codeit.otboo.global.kafka;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.service.NotificationEventService;
import com.codeit.otboo.domain.sse.event.*;
import com.codeit.otboo.global.kafka.event.MultipleNotificationSseKafkaEvent;
import com.codeit.otboo.global.kafka.event.NotificationBatchSseKafkaEvent;
import com.codeit.otboo.global.kafka.event.NotificationSseKafkaEvent;
import com.codeit.otboo.global.websocket.event.DirectMessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationEventService notificationEventService;
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * WebSocket 실시간 채팅 전달용 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DirectMessageCreatedEvent event) {
        DirectMessageResponse directMessageResponse = event.getData();
        String directMessageKey = DirectMessageKeyGenerator.generate(directMessageResponse);

        kafkaEventPublisher.publish(
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
        NotificationDto notificationDto = notificationEventService.createSingleNotification(
                event.getUserId(),
                event.getTitle(),
                event.getContent()
        );

        kafkaEventPublisher.publish(
                KafkaTopics.REALTIME_NOTIFICATION_SINGLE,
                new NotificationSseKafkaEvent(notificationDto),
                notificationDto.receiverId().toString()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FollowSseEvent event) {
        NotificationDto notificationDto = notificationEventService.createSingleNotification(
                event.getUserId(),
                event.getTitle(),
                event.getContent()
        );

        kafkaEventPublisher.publish(
                KafkaTopics.REALTIME_NOTIFICATION_SINGLE,
                new NotificationSseKafkaEvent(notificationDto),
                notificationDto.receiverId().toString()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CommentCreatedEvent event) {
        NotificationDto notificationDto = notificationEventService.createSingleNotification(
                event.getReceiverId(),
                event.getTitle(),
                event.getContent()
        );

        kafkaEventPublisher.publish(
                KafkaTopics.REALTIME_NOTIFICATION_SINGLE,
                new NotificationSseKafkaEvent(notificationDto),
                notificationDto.receiverId().toString()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FeedLikedEvent event) {
        NotificationDto notificationDto = notificationEventService.createSingleNotification(
                event.getReceiverId(),
                event.getTitle(),
                event.getContent()
        );

        kafkaEventPublisher.publish(
                KafkaTopics.REALTIME_NOTIFICATION_SINGLE,
                new NotificationSseKafkaEvent(notificationDto),
                notificationDto.receiverId().toString()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserRoleUpdatedEvent event) {
        NotificationDto notificationDto = notificationEventService.createSingleNotification(
                event.getReceiverId(),
                event.getTitle(),
                event.getContent()
        );

        kafkaEventPublisher.publish(
                KafkaTopics.REALTIME_NOTIFICATION_SINGLE,
                new NotificationSseKafkaEvent(notificationDto),
                notificationDto.receiverId().toString()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FeedCreatedEvent event) {
        List<NotificationDto> notificationDtos = notificationEventService.createMultipleNotifications(
                event.getReceiverIds(),
                event.getTitle(),
                event.getContent()
        );

        notificationDtos
                .forEach(
                        dto -> {
                            kafkaEventPublisher.publish(
                                    KafkaTopics.REALTIME_NOTIFICATION_SINGLE,
                                    new NotificationSseKafkaEvent(dto),
                                    dto.receiverId().toString()
                            );
                        }
                );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ClothesAttributeDefSseEvent event) {
        List<NotificationDto> notificationDtos = notificationEventService.createMultipleNotificationAllByReceivers(
                event.getTitle(),
                event.getContent()
        );

        notificationDtos
                .forEach(
                        dto -> {
                            kafkaEventPublisher.publish(
                                    KafkaTopics.REALTIME_NOTIFICATION_SINGLE,
                                    new NotificationSseKafkaEvent(dto),
                                    dto.receiverId().toString()
                            );
                        }
                );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(WeatherSseEvent event) {
        List<NotificationDto> notificationDtos = notificationEventService.createNotificationsFromCommands(event.notificationCommands());

        kafkaEventPublisher.publish(
                KafkaTopics.REALTIME_NOTIFICATION_BATCH,
                new NotificationBatchSseKafkaEvent(notificationDtos)
        );
    }
}
