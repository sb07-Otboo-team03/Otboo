package com.codeit.otboo.domain.directmessage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageDto (
    UUID id,
    LocalDateTime createdAt,

    UUID senderId,
    String senderName,
    UUID senderProfileImageId,

    UUID receiverId,
    String receiverName,
    UUID receiverProfileImageId,

    String content
) {
}