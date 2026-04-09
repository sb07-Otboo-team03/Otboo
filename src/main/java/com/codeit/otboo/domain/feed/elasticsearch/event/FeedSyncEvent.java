package com.codeit.otboo.domain.feed.elasticsearch.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedSyncEvent(
        UUID feedId,
        String content,
        String skyStatus,
        String precipitationType,
        UUID authorId,
        LocalDateTime createdAt,
        Long likeCount
) {
}
