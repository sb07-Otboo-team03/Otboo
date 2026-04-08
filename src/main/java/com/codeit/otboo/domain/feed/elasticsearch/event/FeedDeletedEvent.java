package com.codeit.otboo.domain.feed.elasticsearch.event;

import java.util.UUID;

public record FeedDeletedEvent(
        UUID feedId
) {
}
