package com.codeit.otboo.domain.sse.listener;

import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.sse.event.*;
import com.codeit.otboo.domain.sse.service.SseService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
//@Component
public class SseRequiredEventListener {

    private final SseService sseService;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

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

    public Notification getNotification(UUID userId, BaseSseEvent event) {

        User user = userService.getUser(userId);

        return Notification.builder()
            .title(event.getTitle())
            .content(event.getContent())
            .level(NotificationLevel.INFO)
            .receiver(user)
            .build();
    }

    public List<Notification> getNotifications(String title, String content, List<User> users) {

        return users
            .stream()
            .map(user -> Notification.builder()
                .title(title)
                .content(content)
                .level(NotificationLevel.INFO)
                .receiver(user)
                .build())
            .toList();
    }

    @Async
    @TransactionalEventListener
    public void on(DirectMessageSseEvent event) {

        Notification notification = getNotification(event.getUserId(), event);
        sendSseEvent(List.of(notification));
    }

    @Async
    @TransactionalEventListener
    public void on(FollowSseEvent event) {

        Notification notification = getNotification(event.getUserId(), event);
        sendSseEvent(List.of(notification));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CommentCreatedEvent event) {

        Notification notification = getNotification(event.getReceiverId(), event);
        sendSseEvent(List.of(notification));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FeedLikedEvent event) {

        String title = event.getTitle();
        String content = event.getContent();

        Notification notification = getNotification(event.getReceiverId(), event);
        sendSseEvent(List.of(notification));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FeedCreatedEvent event) {

        String title = event.getTitle();
        String content = event.getContent();
        List<User> users = userService.getAllUserByIds(event.getReceiverIds());

        List<Notification> notificationList = getNotifications(title, content, users);
        sendSseEvent(notificationList);
    }

    @Async
    @TransactionalEventListener
    public void on(ClothesAttributeDefSseEvent event) {

        String title = event.getTitle();
        String content = event.getContent();
        List<User> users = userService.getAllUsers();

        List<Notification> notificationList = getNotifications(title, content, users);
        sendSseEvent(notificationList);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserRoleUpdatedEvent event) {
        Notification notification = getNotification(event.getReceiverId(), event);
        sendSseEvent(List.of(notification));
    }

    @Async
    @TransactionalEventListener
    public void on(WeatherSseEvent event) {
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
}
