package com.codeit.otboo.domain.feed.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentsRequest(
    String cursor,
    UUID idAfter,

    @NotNull
    Integer limit
) {
}
