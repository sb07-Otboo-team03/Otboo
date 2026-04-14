package com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ClothesAttributeRequest(
    UUID definitionId,
    String value
){}
