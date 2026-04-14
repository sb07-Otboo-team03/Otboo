package com.codeit.otboo.domain.notification.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationDto(
    UUID id,
    LocalDateTime createdAt,
    UUID receiverId,
    String title,
    String content,
    NotificationLevel level
) {
}
