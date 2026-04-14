package com.codeit.otboo.domain.feed.elasticsearch.event;

import java.util.UUID;

public record LikeUpdatedEvent(
        UUID feedId,
        Long updatedLikeCount
) {
}
