package com.codeit.otboo.domain.clothes.dto.response;

import java.util.UUID;

public record ClothesAttributeResponse (
    UUID definitionId,
    String value
){}
