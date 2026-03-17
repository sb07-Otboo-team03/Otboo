package com.codeit.otboo.domain.clothes.management.dto.request;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.response.ClothesAttributeResponse;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;

import java.util.List;

public record ClothesUpdateRequest(
        String name,
        ClothesType type,
        List<ClothesAttributeResponse> attributes
) {
}
