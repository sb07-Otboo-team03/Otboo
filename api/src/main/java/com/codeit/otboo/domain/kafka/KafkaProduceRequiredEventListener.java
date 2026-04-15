package com.codeit.otboo.domain.kafka;

import com.codeit.otboo.domain.kafka.event.NotificationBatchSseKafkaEvent;
import com.codeit.otboo.domain.kafka.event.NotificationSseKafkaEvent;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.sse.event.ClothesAttributeDefSseEvent;
import com.codeit.otboo.domain.sse.event.CommentCreatedEvent;
import com.codeit.otboo.domain.sse.event.DirectMessageSseEvent;
import com.codeit.otboo.domain.sse.event.FeedCreatedEvent;
import com.codeit.otboo.domain.sse.event.FeedLikedEvent;
import com.codeit.otboo.domain.sse.event.FollowSseEvent;
import com.codeit.otboo.domain.sse.event.UserRoleUpdatedEvent;
import com.codeit.otboo.domain.sse.event.WeatherSseEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.domain.websocket.event.DirectMessageCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    @Async
    @TransactionalEventListener
    public void on(DirectMessageCreatedEvent event) {
        sendToKafka(event);
    }

    @Async
    @TransactionalEventListener
    public void on(DirectMessageSseEvent event) {
        Notification saved = createNotification(
                event.getUserId(),
                event.getTitle(),
                event.getContent()
        );

        NotificationDto dto = notificationMapper.toDto(saved);
        sendToKafka(new NotificationSseKafkaEvent(dto));
    }

    @Async
    @TransactionalEventListener
    public void on(FollowSseEvent event) {
        Notification saved = createNotification(
                event.getUserId(),
                event.getTitle(),
                event.getContent()
        );

        NotificationDto dto = notificationMapper.toDto(saved);
        sendToKafka(new NotificationSseKafkaEvent(dto));
    }

    @Async
    @TransactionalEventListener
    public void on(CommentCreatedEvent event) {
        Notification saved = createNotification(
                event.getReceiverId(),
                event.getTitle(),
                event.getContent()
        );

        NotificationDto dto = notificationMapper.toDto(saved);
        sendToKafka(new NotificationSseKafkaEvent(dto));
    }

    @Async
    @TransactionalEventListener
    public void on(FeedLikedEvent event) {
        Notification saved = createNotification(
                event.getReceiverId(),
                event.getTitle(),
                event.getContent()
        );

        NotificationDto dto = notificationMapper.toDto(saved);
        sendToKafka(new NotificationSseKafkaEvent(dto));
    }

    @Async
    @TransactionalEventListener
    public void on(UserRoleUpdatedEvent event) {
        Notification saved = createNotification(
                event.getReceiverId(),
                event.getTitle(),
                event.getContent()
        );

        NotificationDto dto = notificationMapper.toDto(saved);
        sendToKafka(new NotificationSseKafkaEvent(dto));
    }

    @Async
    @TransactionalEventListener
    public void on(FeedCreatedEvent event) {
        List<User> users = userService.getAllUserByIds(event.getReceiverIds());

        List<NotificationDto> dtos = users.stream()
                .map(user -> Notification.builder()
                        .title(event.getTitle())
                        .content(event.getContent())
                        .level(NotificationLevel.INFO)
                        .receiver(user)
                        .build())
                .map(notificationService::create)
                .map(notificationMapper::toDto)
                .toList();

        sendToKafka(new NotificationBatchSseKafkaEvent(dtos));
    }

    @Async
    @TransactionalEventListener
    public void on(ClothesAttributeDefSseEvent event) {
        List<User> users = userService.getAllUsers();

        List<NotificationDto> dtos = users.stream()
                .map(user -> Notification.builder()
                        .title(event.getTitle())
                        .content(event.getContent())
                        .level(NotificationLevel.INFO)
                        .receiver(user)
                        .build())
                .map(notificationService::create)
                .map(notificationMapper::toDto)
                .toList();

        sendToKafka(new NotificationBatchSseKafkaEvent(dtos));
    }

    @Async
    @TransactionalEventListener
    public void on(WeatherSseEvent event) {
        List<NotificationDto> dtos = event.notificationCommands().stream()
                .map(notificationService::create)
                .map(notificationMapper::toDto)
                .toList();

        sendToKafka(new NotificationBatchSseKafkaEvent(dtos));
    }

    private Notification createNotification(UUID userId, String title, String content) {
        User user = userService.getUser(userId);

        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .level(NotificationLevel.INFO)
                .receiver(user)
                .build();

        return notificationService.create(notification);
    }

    private <T> void sendToKafka(T event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            String topic = "otboo." + event.getClass().getSimpleName();
            kafkaTemplate.send(topic, message);
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send event to Kafka", e);
            throw new RuntimeException(e);
        }
    }
}
