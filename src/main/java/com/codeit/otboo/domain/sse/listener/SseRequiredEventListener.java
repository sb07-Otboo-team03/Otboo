package com.codeit.otboo.domain.sse.listener;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.sse.event.FeedCreatedEvent;
import com.codeit.otboo.domain.sse.event.SseEvent;
import com.codeit.otboo.domain.sse.service.SseService;
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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(SseEvent event) {
        NotificationDto notification = NotificationDto.from(event);
        UUID receiverId = notification.receiverId();
        sseService.send(Set.of(receiverId), "notifications", notification);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FeedCreatedEvent event) {
        for(UUID receiverId : event.receiverIds()) {
            NotificationDto notificationDto = NotificationDto.from(event, receiverId);
            sseService.send(Set.of(receiverId), "notifications", notificationDto);
        }
    }
}
