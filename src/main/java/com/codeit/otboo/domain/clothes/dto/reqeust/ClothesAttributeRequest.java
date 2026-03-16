package com.codeit.otboo.domain.clothes.dto.reqeust;

import java.util.UUID;

public record ClothesAttributeRequest(
    UUID definitionId,
    String value
){}
