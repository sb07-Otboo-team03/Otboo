package com.codeit.otboo.domain.clothes.management.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ClothesUrlRequest(
        @NotBlank
        String url
) {}
