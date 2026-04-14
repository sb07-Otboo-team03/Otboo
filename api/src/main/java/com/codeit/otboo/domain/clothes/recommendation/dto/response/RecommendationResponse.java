package com.codeit.otboo.domain.clothes.recommendation.dto.response;

import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record RecommendationResponse(
        UUID weatherId,
        UUID userId,
        List<ClothesResponse> clothes
) {
}
