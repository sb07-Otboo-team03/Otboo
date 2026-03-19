package com.codeit.otboo.domain.feed.dto.request;

import com.codeit.otboo.domain.feed.dto.type.SortBy;
import com.codeit.otboo.global.slice.dto.SortDirection;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public record FeedSearchRequest(
    String cursor,
    UUID idAfter,
    Integer limit,
    String sortBy,
    SortDirection sortDirection,
    String keywordLike,
    SkyStatus skyStatusEqual,
    PrecipitationType precipitationTypeEqual
) {
    public FeedSearchRequest {
        limit = Objects.requireNonNullElse(limit, 10);
        sortBy = Objects.requireNonNullElse(sortBy, SortBy.CREATED_AT.name());
        sortDirection = Objects.requireNonNullElse(sortDirection, SortDirection.DESCENDING);
    }
}
