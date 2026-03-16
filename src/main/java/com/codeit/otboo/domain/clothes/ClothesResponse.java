package com.codeit.otboo.domain.clothes;

import com.codeit.otboo.domain.clothes.entity.ClothesType;

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
