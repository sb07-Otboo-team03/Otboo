package com.codeit.otboo.domain.notification.dto;

import com.codeit.otboo.domain.sse.event.FeedCreatedEvent;
import com.codeit.otboo.domain.sse.event.SseEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationDto(
    UUID id,
    LocalDateTime createdAt,
    UUID receiverId,
    String title,
    String content,
    NotificationLevel level
) {
    public static NotificationDto from(SseEvent event) {
        return  NotificationDto.builder()
            .id(event.id())
            .createdAt(event.createdAt())
            .receiverId(event.receiverId())
            .title(event.title())
            .content(event.content())
            .level(event.level())
            .build();
    }

    public static NotificationDto from(FeedCreatedEvent event, UUID receiverId ) {
        return  NotificationDto.builder()
            .id(event.feedId())
            .createdAt(event.createdAt())
            .receiverId(receiverId)
            .title(event.authorName() + "님이 새로운 피드를 작성했어요.")
            .content(event.content())
            .level(NotificationLevel.INFO)
            .build();
    }
}
