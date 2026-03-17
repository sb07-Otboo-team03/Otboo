package com.codeit.otboo.domain.directmessage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CursorRequest (
    String cursor,

    UUID idAfter,

    @NotNull
    @Min(1)
    Integer limit
) {
}
