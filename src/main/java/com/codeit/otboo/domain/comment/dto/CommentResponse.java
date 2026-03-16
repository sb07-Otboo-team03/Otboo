package com.codeit.otboo.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
    UUID id,
    LocalDateTime createdAt,
    UUID feedId,
    AuthorResponse author,
    String content
) {}