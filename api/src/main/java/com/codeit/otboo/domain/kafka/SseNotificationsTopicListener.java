package com.codeit.otboo.domain.kafka;

import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.sse.event.ClothesAttributeDefSseEvent;
import com.codeit.otboo.domain.sse.event.FeedCreatedEvent;
import com.codeit.otboo.domain.sse.event.WeatherSseEvent;
import com.codeit.otboo.domain.sse.service.SseService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseNotificationsTopicListener {

    private final SseService sseService;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    private final ObjectMapper objectMapper;

    private void sendSseEventWithSave(List<Notification> notification) {

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

    private void sendSseEvent(List<Notification> notification) {

        notification.stream()
            .map(notificationMapper::toDto)
            .forEach(notificationDto ->
                sseService.send(
                    Set.of(notificationDto.receiverId()),
                    "notifications",
                    notificationDto)
            );
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

    @KafkaListener(topics = "otboo.FeedCreatedEvent", groupId = "sse-${random.uuid}")
    public void onFeedCreatedEvent(String kafkaEvent) {
        try {
            FeedCreatedEvent event =
                objectMapper.readValue(kafkaEvent, FeedCreatedEvent.class);

            String title = event.getTitle();
            String content = event.getContent();
            List<User> users = userService.getAllUserByIds(event.getReceiverIds());

            List<Notification> notificationList = getNotifications(title, content, users);
            sendSseEventWithSave(notificationList);
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
            sendSseEventWithSave(notificationList);
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
