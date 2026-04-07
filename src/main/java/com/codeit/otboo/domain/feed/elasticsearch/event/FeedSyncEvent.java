package com.codeit.otboo.domain.feed.elasticsearch.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedSyncEvent(
        UUID feedId,
        String content,
        String skyStatus,
        String precipitationType,
        LocalDateTime createdAt,
        Long likeCount
) {
}
