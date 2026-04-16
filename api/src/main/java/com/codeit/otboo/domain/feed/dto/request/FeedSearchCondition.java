package com.codeit.otboo.domain.feed.dto.request;

import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import com.codeit.otboo.global.slice.dto.SortDirection;
import lombok.Builder;

import java.util.UUID;

@Builder
public record FeedSearchCondition(
    String cursor,
    UUID idAfter,
    int limit,
    String sortBy,
    SortDirection sortDirection,
    String keywordLike,
    SkyStatus skyStatusEqual,
    PrecipitationType precipitationTypeEqual,
    UUID authorIdEqual
) {
    public static FeedSearchCondition from(FeedSearchRequest request) {
        return new FeedSearchCondition(
                request.cursor(),
                request.idAfter(),
                request.limit(),
                request.sortBy(),
                request.sortDirection(),
                request.keywordLike(),
                request.skyStatusEqual(),
                request.precipitationTypeEqual(),
                request.authorIdEqual()
        );
    }
}
