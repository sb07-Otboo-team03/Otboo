package com.codeit.otboo.domain.clothes;

import com.codeit.otboo.domain.clothes.entity.ClothesType;

import java.util.List;

public record ClothesUpdateRequest(
        String name,
        ClothesType type,
        List<ClothesAttributeResponse> attributes
) {
}
