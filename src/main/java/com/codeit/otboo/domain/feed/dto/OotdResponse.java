package com.codeit.otboo.domain.feed.dto;

import com.codeit.otboo.domain.feed.dto.type.ClothesType;
import java.util.List;
import java.util.UUID;

public record OotdResponse(
    UUID clothesId,
    String name,
    String imageUrl,
    ClothesType type,
    List<FeedClothesAttributeResponse> attributes
) {
}