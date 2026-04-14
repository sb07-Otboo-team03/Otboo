package com.codeit.otboo.domain.notification.dto;

import java.util.UUID;

public record NotificationCreateCommand(
        UUID receiverId,
        String title,
        String content,
        NotificationLevel level
) {
}
