package com.codeit.otboo.domain.clothes.recommendation.controller;

import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.service.RecommendationService;
import com.codeit.otboo.global.security.OtbooUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<RecommendationResponse> recommend(
            @RequestParam UUID weatherId,
            Authentication authentication
            ) {
        OtbooUserDetails userDetails = (OtbooUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                recommendationService.recommend(weatherId, userDetails.getUserResponse().id())
        );
    }

}
