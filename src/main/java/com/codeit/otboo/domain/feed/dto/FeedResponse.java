package com.codeit.otboo.domain.feed.dto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FeedResponse(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    AuthorResponse authorResponse,
    FeedWeatherResponse weatherResponse,
    List<OotdResponse> ootds,
    String content,
    Long likeCount,
    int commentCount,
    Boolean likedByMe
) {
}