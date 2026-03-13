package com.codeit.otboo.domain.clothes;

import java.util.UUID;

public record ClothesAttributeResponse (
    UUID definitionId,
    String value
){}
