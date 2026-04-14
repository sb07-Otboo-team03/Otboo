package com.codeit.otboo.domain.comment.dto;

import com.codeit.otboo.global.slice.dto.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.util.Objects;
import java.util.UUID;

public record CommentSearchRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        UUID feedId,
        String cursor,
        UUID idAfter,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(1)
        Integer limit
) {
    public CommentSearchRequest {
        limit = Objects.requireNonNullElse(limit, 20);
    }
}
