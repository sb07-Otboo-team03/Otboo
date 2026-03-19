package com.codeit.otboo.domain.notification.dto;

import com.codeit.otboo.domain.notification.entity.Level;

import com.codeit.otboo.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        LocalDateTime createdAt,
        UUID receiverId,
        String title,
        String content,
        Level level
) {
    public static NotificationResponse toDto(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getCreatedAt(),
            notification.getReceiver().getId(),
            notification.getTitle(),
            notification.getContent(),
            notification.getLevel()
        );
    }
}
