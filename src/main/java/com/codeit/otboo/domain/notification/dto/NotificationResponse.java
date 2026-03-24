package com.codeit.otboo.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        LocalDateTime createdAt,
        UUID receiverId,
        String title,
        String content,
        NotificationLevel level
) {
}
