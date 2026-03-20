package com.codeit.otboo.domain.clothes.management.dto.response;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeWithDefResponse;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;

import java.util.ArrayList;
import java.util.UUID;

public record ClothesResponse (
        UUID id,
        UUID ownerId,
        String name,
        String imageUrl,
        ClothesType type,
        ArrayList<ClothesAttributeWithDefResponse> attributes
){}