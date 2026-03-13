package com.codeit.otboo.domain.directmessage.dto.response;

import com.codeit.otboo.domain.user.dto.response.UserResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageResponse(
        UUID directMessageId,
        LocalDateTime createdAt,
        UserResponse sender,
        UserResponse receiver,
        String content
) {}
