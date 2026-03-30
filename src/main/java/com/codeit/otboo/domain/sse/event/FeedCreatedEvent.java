package com.codeit.otboo.domain.sse.event;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder
public record FeedCreatedEvent(
        UUID feedId,
        String authorName,
        String content,
        LocalDateTime createdAt,
        Set<UUID> receiverIds
) {
}
