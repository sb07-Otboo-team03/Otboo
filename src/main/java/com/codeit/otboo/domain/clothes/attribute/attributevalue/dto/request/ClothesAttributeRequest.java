package com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.request;

import java.util.UUID;

public record ClothesAttributeRequest(
    UUID definitionId,
    String value
){}
