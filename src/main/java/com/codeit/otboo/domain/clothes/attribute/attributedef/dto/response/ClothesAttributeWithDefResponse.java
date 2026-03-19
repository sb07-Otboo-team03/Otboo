package com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ClothesAttributeWithDefResponse (
    UUID definitionId,
    String definitionName,
    List<String> selectableValue,
    String value
){}
