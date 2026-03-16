package com.codeit.otboo.domain.comment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentCursorRequest(
    String cursor,
    UUID idAfter,

    @NotNull
    Integer limit
) {
}
