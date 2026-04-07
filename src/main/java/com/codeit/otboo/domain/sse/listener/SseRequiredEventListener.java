package com.codeit.otboo.domain.sse.listener;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.sse.event.*;
import com.codeit.otboo.domain.sse.service.SseService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseRequiredEventListener {

    private final SseService sseService;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    private void sendSseEvent(List<Notification> notification) {

        notification.stream()
            .map(notificationService::createSseEvent)
            .map(notificationMapper::toDto)
            .forEach(notificationDto ->
                sseService.send(
                    Set.of(notificationDto.receiverId()),
                    "notifications",
                    notificationDto)
            );
    }

    @Async
    @TransactionalEventListener
    public void on(DirectMessageSseEvent event) {
        sendSseEvent(event.notificationList);
    }

    @Async
    @TransactionalEventListener
    public void on(FollowSseEvent event) {
        sendSseEvent(event.notificationList);
    }

    @Async
    @TransactionalEventListener
    public void on(WeatherSseEvent event) {
        sendSseEvent(event.notificationList);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FeedCreatedEvent event) {
        for(UUID receiverId : event.receiverIds()) {
            NotificationDto notificationDto = NotificationDto.from(event, receiverId);
            sseService.send(Set.of(receiverId), "notifications", notificationDto);
        }
    }

    // TODO: 삭제 예정
    @Async
    @TransactionalEventListener
    public void on(SseEvent event) {
        NotificationDto notificationDto = event.getData();
        UUID receiverId = notificationDto.receiverId();
        sseService.send(Set.of(receiverId), "notifications", notificationDto);
    }

}
