package com.codeit.otboo.domain.clothes.recommendation.service;

import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;

import java.util.UUID;

public interface RecommendationService {

    RecommendationResponse recommend(UUID weatherId, UUID userId);

}
