package com.codeit.otboo.domain.feed.dto.response;
import com.codeit.otboo.domain.comment.dto.AuthorResponse;
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
    AuthorResponse authorResponse,
    WeatherSummaryResponse weatherResponse,
    List<FeedOotdResponse> ootds,
    String content,
    Long likeCount,
    int commentCount,
    Boolean likedByMe
) {
}