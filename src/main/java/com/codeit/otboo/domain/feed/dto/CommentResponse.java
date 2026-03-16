package com.codeit.otboo.domain.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
    UUID id,
    Instant createdAt,
    UUID feedId,
    AuthorResponse author,
    String content
) {}