package com.codeit.otboo.domain.feed.dto;

import com.codeit.otboo.domain.feed.dto.type.SortBy;
import com.codeit.otboo.global.slice.dto.SortDirection;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FeedCursorRequest(
    String cursor,
    UUID idAfter,

    @NotNull
    int limit,

    @NotNull
    SortBy sortBy,

    @NotNull
    SortDirection sortDirection,

    String keywordLike,
    SkyStatus skyStatusEqual,
    PrecipitationType precipitationTypeEqual,
    UUID authorIdEqual
) {
}
