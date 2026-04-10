package com.codeit.otboo.domain.kafka;

import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;

import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.sse.event.BaseSseEvent;
import com.codeit.otboo.domain.sse.event.ClothesAttributeDefSseEvent;
import com.codeit.otboo.domain.sse.event.CommentCreatedEvent;
import com.codeit.otboo.domain.sse.event.DirectMessageSseEvent;
import com.codeit.otboo.domain.sse.event.FeedCreatedEvent;
import com.codeit.otboo.domain.sse.event.FeedLikedEvent;
import com.codeit.otboo.domain.sse.event.FollowSseEvent;
import com.codeit.otboo.domain.sse.event.UserRoleUpdatedEvent;
import com.codeit.otboo.domain.sse.event.WeatherSseEvent;
import com.codeit.otboo.domain.sse.service.SseService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseRequiredTopicListener {

    private final SseService sseService;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    private final ObjectMapper objectMapper;

    private void sendSseEvent(List<Notification> notification) {

        notification.stream()
            .map(notificationService::create)
            .map(notificationMapper::toDto)
            .forEach(notificationDto ->
                sseService.send(
                    Set.of(notificationDto.receiverId()),
                    "notifications",
                    notificationDto)
            );
    }

    private Notification getNotification(UUID userId, BaseSseEvent event) {

        User user = userService.getUser(userId);

        return Notification.builder()
            .title(event.getTitle())
            .content(event.getContent())
            .level(NotificationLevel.INFO)
            .receiver(user)
            .build();
    }

    private List<Notification> getNotifications(String title, String content, List<User> users) {

        return users.stream()
            .map(user -> Notification.builder()
                .title(title)
                .content(content)
                .level(NotificationLevel.INFO)
                .receiver(user)
                .build())
            .toList();
    }

    @KafkaListener(topics = "otboo.DirectMessageSseEvent", groupId = "sse-${random.uuid}")
    public void onDirectMessageSseEvent(String kafkaEvent) {
        try {
            DirectMessageSseEvent event =
                objectMapper.readValue(kafkaEvent, DirectMessageSseEvent.class);

            Notification notification = getNotification(event.getUserId(), event);
            sendSseEvent(List.of(notification));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "otboo.FollowSseEvent", groupId = "sse-${random.uuid}")
    public void onFollowSseEvent(String kafkaEvent) {
        try {
            FollowSseEvent event =
                objectMapper.readValue(kafkaEvent, FollowSseEvent.class);

            Notification notification = getNotification(event.getUserId(), event);
            sendSseEvent(List.of(notification));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "otboo.CommentCreatedEvent", groupId = "sse-${random.uuid}")
    public void onCommentCreatedEvent(String kafkaEvent) {
        try {
            CommentCreatedEvent event =
                objectMapper.readValue(kafkaEvent, CommentCreatedEvent.class);

            Notification notification = getNotification(event.getReceiverId(), event);
            sendSseEvent(List.of(notification));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "otboo.FeedLikedEvent", groupId = "sse-${random.uuid}")
    public void onFeedLikedEvent(String kafkaEvent) {
        try {
            FeedLikedEvent event =
                objectMapper.readValue(kafkaEvent, FeedLikedEvent.class);

            String title = event.getTitle();
            String content = event.getContent();

            Notification notification = getNotification(event.getReceiverId(), event);
            sendSseEvent(List.of(notification));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "otboo.FeedCreatedEvent", groupId = "sse-${random.uuid}")
    public void onFeedCreatedEvent(String kafkaEvent) {
        try {
            FeedCreatedEvent event =
                objectMapper.readValue(kafkaEvent, FeedCreatedEvent.class);

            String title = event.getTitle();
            String content = event.getContent();
            List<User> users = userService.getAllUserByIds(event.getReceiverIds());

            List<Notification> notificationList = getNotifications(title, content, users);
            sendSseEvent(notificationList);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "otboo.ClothesAttributeDefSseEvent", groupId = "sse-${random.uuid}")
    public void onClothesAttributeDefSseEvent(String kafkaEvent) {
        try {
            ClothesAttributeDefSseEvent event =
                objectMapper.readValue(kafkaEvent, ClothesAttributeDefSseEvent.class);

            String title = event.getTitle();
            String content = event.getContent();
            List<User> users = userService.getAllUsers();

            List<Notification> notificationList = getNotifications(title, content, users);
            sendSseEvent(notificationList);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @KafkaListener(topics = "otboo.UserRoleUpdatedEvent", groupId = "sse-${random.uuid}")
    public void onUserRoleUpdatedEvent(String kafkaEvent) {
        try {
            UserRoleUpdatedEvent event =
                objectMapper.readValue(kafkaEvent, UserRoleUpdatedEvent.class);

            Notification notification = getNotification(event.getReceiverId(), event);
            sendSseEvent(List.of(notification));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "otboo.WeatherSseEvent", groupId = "sse-${random.uuid}")
    public void onWeatherSseEvent(String kafkaEvent) {
        try {
            WeatherSseEvent event =
                objectMapper.readValue(kafkaEvent, WeatherSseEvent.class);

            event.notificationCommands().stream()
                .map(notificationService::create)
                .map(notificationMapper::toDto)
                .forEach(notificationDto ->
                    sseService.send(
                        Set.of(notificationDto.receiverId()),
                        "notifications",
                        notificationDto
                    )
                );
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
