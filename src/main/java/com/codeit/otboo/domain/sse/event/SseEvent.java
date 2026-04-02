package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
public record SseEvent(
    UUID id,
    LocalDateTime createdAt,
    UUID receiverId,
    String title,
    String content,
    NotificationLevel level
) {
    public static SseEvent of(NotificationDto data) {
        return SseEvent.builder()
            .id(data.id())
            .createdAt(data.createdAt())
            .receiverId(data.receiverId())
            .title(data.title())
            .content(data.content())
            .level(NotificationLevel.INFO)
            .build();
    }
}