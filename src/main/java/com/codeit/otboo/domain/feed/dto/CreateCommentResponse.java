package com.codeit.otboo.domain.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCommentResponse(
    @NotNull
    UUID feedId,

    @NotNull
    UUID authorId,

    @NotBlank
    String content
) {
}
