package com.codeit.otboo.domain.feed.dto.response;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import com.codeit.otboo.domain.weather.dto.response.WeatherSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FeedResponse(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    UserSummaryResponse userResponse,
    WeatherSummaryResponse weatherResponse,
    List<FeedOotdResponse> ootds,
    String content,
    Long likeCount,
    int commentCount,
    Boolean likedByMe
) {
}