package com.codeit.otboo.domain.directmessage.dto;

import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageResponse(
        UUID id,
        LocalDateTime createdAt,
        UserSummaryResponse sender,
        UserSummaryResponse receiver,
        String content
) {}
