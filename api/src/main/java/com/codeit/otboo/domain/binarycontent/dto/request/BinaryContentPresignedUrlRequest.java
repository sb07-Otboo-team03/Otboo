package com.codeit.otboo.domain.binarycontent.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BinaryContentPresignedUrlRequest(
        @NotBlank
        String fileName,

        @NotBlank
        String contentType,

        @Min(0)
        Long size
) {
}
