package com.codeit.otboo.domain.comment.dto;

import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CommentResponse(
        UUID id,
        LocalDateTime createdAt,
        UUID feedId,
        UserSummaryResponse author,
        String content
) {}