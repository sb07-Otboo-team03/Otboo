package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import java.time.LocalDateTime;

// 의상 속성 추가
public class ClothesAttributeCreateEvent extends SseEvent {
    public ClothesAttributeCreateEvent(NotificationDto data, LocalDateTime createdAt) {
        super(data, createdAt);
    }
}
