package com.codeit.otboo.domain.feed.dto.response;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeWithDefResponse;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;

import java.util.List;
import java.util.UUID;

public record FeedOotdResponse(
        UUID clothesId,
        String name,
        String imageUrl,
        ClothesType type,
        List<ClothesAttributeWithDefResponse> attributes
) {
}