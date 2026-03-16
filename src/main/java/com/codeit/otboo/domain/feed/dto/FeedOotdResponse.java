package com.codeit.otboo.domain.feed.dto;

import com.codeit.otboo.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;

public record FeedOotdResponse(
    UUID clothesId,
    String name,
    String imageUrl,
    ClothesType type,
    List<FeedClothesAttributeResponse> attributes
) {
}