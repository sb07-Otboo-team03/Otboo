package com.codeit.otboo.domain.feed.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record AuthorResponse(
    UUID userId,
    String name,
    String profileImageUrl
) {
}
