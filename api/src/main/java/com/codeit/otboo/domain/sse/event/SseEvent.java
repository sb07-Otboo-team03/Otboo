package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SseEvent {

    private final NotificationDto data;

    public SseEvent(final NotificationDto data) {
        this.data = data;
    }
}