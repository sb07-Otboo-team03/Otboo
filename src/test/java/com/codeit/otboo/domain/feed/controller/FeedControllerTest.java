package com.codeit.otboo.domain.feed.controller;

import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.service.FeedService;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.weather.dto.response.WeatherSummaryResponse;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.security.jwt.JwtProvider;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )})
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private FeedService feedService;

    private UUID userId;
    private UUID feedId;
    private UUID weatherId;
    private List<UUID> clothesId;
    private FeedResponse dto;
    private OtbooUserDetails userDetails;

    @BeforeEach
    void setup() {


        userId = UUID.randomUUID();
        feedId = UUID.randomUUID();
        weatherId = UUID.randomUUID();
        clothesId = List.of(UUID.randomUUID(), UUID.randomUUID());
        WeatherSummaryResponse weather = new WeatherSummaryResponse(weatherId, null, null, null);
        dto = FeedResponse.builder().id(feedId).weather(weather).content("content").build();
        UserResponse userDto = UserResponse.builder().id(userId).role(Role.USER).build();
        userDetails = new OtbooUserDetails(userDto, "otboo123");
    }

    @Nested
    @DisplayName("피드 생성")
    class FeedCreate {

        @Test
        @DisplayName("피드 생성 성공")
        void createFeed_Success() throws Exception {
            // given
            FeedCreateRequest request = new FeedCreateRequest(userId, weatherId, clothesId, "content");
            given(feedService.createFeed(request)).willReturn(dto);

            // when & then
            mockMvc.perform(post("/api/feeds")
                            .with(csrf())
                            .with(user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(feedId.toString()));
        }

        @Test
        @DisplayName("피드 생성 실패 - validation")
        void createFeed_Fail_Validation() throws Exception {
            // given
            FeedCreateRequest request = new FeedCreateRequest(userId, weatherId, null, "content");

            // when & then
            mockMvc.perform(post("/api/feeds")
                            .with(csrf())
                            .with(user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("피드 목록 조회")
    class FeedSearch {

        @Test
        @DisplayName("피드 목록 조회")
        void searchFeedList() throws Exception {
            // given
            List<FeedResponse> feedList = List.of(dto);
            CursorResponse<FeedResponse> page = new CursorResponse<>(feedList, null, null,
                    false, 1L, "createdAt", SortDirection.DESCENDING);

            given(feedService.getAllFeed(any(FeedSearchRequest.class), any())).willReturn(page);

            // when & then
            mockMvc.perform(get("/api/feeds")
                            .with(user(userDetails))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").value(feedId.toString()));
        }
    }

    @Nested
    @DisplayName("피드 수정")
    class FeedUpdate {

        @Test
        @DisplayName("피드 수정 성공")
        void updateFeed_Success() throws Exception {
            // given
            String newContent = "new content";
            FeedUpdateRequest request = new FeedUpdateRequest(newContent);
            FeedResponse updateDto = FeedResponse.builder().id(feedId).content(newContent).build();

            given(feedService.updateFeed(eq(feedId), eq(request), any())).willReturn(updateDto);

            // when & then
            mockMvc.perform(patch("/api/feeds/{feedId}", feedId)
                            .with(csrf())
                            .with(user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(feedId.toString()))
                    .andExpect(jsonPath("$.content").value(newContent));
        }

        @Test
        @DisplayName("피드 수정 실패 - validation")
        void updateFeed_Fail_Validation() throws Exception {
            // given
            FeedUpdateRequest request = new FeedUpdateRequest(null);

            // when & then
            mockMvc.perform(patch("/api/feeds/{feedId}", feedId)
                            .with(csrf())
                            .with(user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("피드 삭제")
    class FeedDelete {

        @Test
        @DisplayName("피드 삭제 성공")
        void deleteFeed_Success() throws Exception {
            // given

            // when & then
            mockMvc.perform(delete("/api/feeds/{feedId}", feedId)
                            .with(csrf())
                            .with(user(userDetails)))
                    .andExpect(status().isNoContent());
            verify(feedService).deleteFeed(eq(feedId), any());
        }
    }
}