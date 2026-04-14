package com.codeit.otboo.domain.clothes.recommendation.integration.slice.web;

import com.codeit.otboo.domain.clothes.recommendation.controller.RecommendationController;
import com.codeit.otboo.domain.clothes.recommendation.dto.response.RecommendationResponse;
import com.codeit.otboo.domain.clothes.recommendation.service.RecommendationService;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecommendationController.class,
        excludeFilters = {
                @ComponentScan.Filter( type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )
        })
@AutoConfigureMockMvc(addFilters = false)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private RecommendationService recommendationService;

    @Test
    @DisplayName("추천 GetMapping 성공")
    void recommendation_success() throws Exception {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RecommendationResponse response = RecommendationResponse.builder()
                .weatherId(weatherId)
                .userId(userId)
                .clothes(List.of())
                .build();

        when(recommendationService.recommend(weatherId, userId))
                .thenReturn(response);

        //가짜 사용자
        OtbooUserDetails userDetails = mock(OtbooUserDetails.class);
        UserResponse userResponse = mock(UserResponse.class);

        when(userResponse.id()).thenReturn(userId);
        when(userDetails.getUserResponse()).thenReturn(userResponse);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of()
        );

        // when & then
        mockMvc.perform(get("/api/recommendations")
                .param("weatherId", weatherId.toString())
                .principal(authentication)
        ).andExpect(status().isOk());

        verify(recommendationService).recommend(weatherId, userId);
    }

    @Test
    @DisplayName("weatherId 없음")
    void recommendation_Fail_NotFound_WeatherId() throws Exception {
        OtbooUserDetails userDetails = mock(OtbooUserDetails.class);
        UserResponse userResponse = mock(UserResponse.class);

        when(userDetails.getUserResponse()).thenReturn(userResponse);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of()
        );

        mockMvc.perform(get("/api/recommendations")
                        .principal(authentication))
                .andExpect(status().isBadRequest());

        verify(recommendationService, never()).recommend(any(), any());
    }
}