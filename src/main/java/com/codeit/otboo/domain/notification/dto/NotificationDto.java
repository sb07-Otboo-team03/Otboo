package com.codeit.otboo.domain.notification.dto;

import com.codeit.otboo.domain.notification.entity.Level;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    LocalDateTime createdAt,
    UUID receiverId,
    String title,
    String content,
    Level level
) {
}
