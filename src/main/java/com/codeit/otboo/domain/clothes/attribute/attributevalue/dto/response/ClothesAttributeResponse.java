package com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ClothesAttributeResponse (
    UUID definitionId,
    String value
){}
