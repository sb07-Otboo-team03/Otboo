package com.codeit.otboo.domain.clothes.management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ClothesCursorPageRequest (
    String cursor,
    String idAfter,
    @Min(1)
    Integer limit,
    String typeEqual,
    @NotNull
    UUID ownerId
){
    public ClothesCursorPageRequest {
        if (limit == null) limit = 20;
    }
}
