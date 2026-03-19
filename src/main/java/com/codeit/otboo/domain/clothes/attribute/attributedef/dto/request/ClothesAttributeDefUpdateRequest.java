package com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record ClothesAttributeDefUpdateRequest(
        String name,
        List<String> selectableValues
) {
}
