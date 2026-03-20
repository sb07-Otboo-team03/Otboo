package com.codeit.otboo.domain.notification.dto;

import com.codeit.otboo.domain.notification.entity.Level;
import com.codeit.otboo.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    LocalDateTime createdAt,
    String title,
    String content,
    Level level,
    User receiver
) {
}
