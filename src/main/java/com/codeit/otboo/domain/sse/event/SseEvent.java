package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SseEvent {

    private final NotificationDto data;
    private final LocalDateTime createdAt;

    public SseEvent(final NotificationDto data, final LocalDateTime createdAt) {
        this.data = data;
        this.createdAt = createdAt;
    }
}