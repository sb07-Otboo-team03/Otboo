package com.codeit.otboo.domain.feed.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthorResponse(
        UUID userId,
        String name,
        String profileImageUrl
) {
}
