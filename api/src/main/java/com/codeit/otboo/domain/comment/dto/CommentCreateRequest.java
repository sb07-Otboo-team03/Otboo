package com.codeit.otboo.domain.comment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentCreateRequest(
    @NotBlank
    String content
) {
}
