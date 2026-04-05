package com.codeit.otboo.domain.follow.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import com.codeit.otboo.domain.follow.controller.FollowController;
import com.codeit.otboo.global.config.SecurityConfig;
import com.codeit.otboo.global.slice.dto.SortDirection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.domain.follow.service.FollowService;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WithMockUser
@DisplayName("🎯Unit Test> FollowController")
@WebMvcTest(
    controllers = FollowController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = SecurityConfig.class
        ),
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class
        )
    }
)
@AutoConfigureMockMvc(addFilters = false)
class FollowControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FollowService followService;


    @Autowired
    ObjectMapper objectMapper;

    private final TestFixture fixture = new TestFixture();

    @Test
    @WithMockUser
    @DisplayName("POST /api/follows - ⭕️ 팔로우 생성")
    void createFollow_OK() throws Exception {
        // given
        FollowCreateRequest request = new FollowCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        FollowResponse response = FollowResponse.builder()
            .id(UUID.randomUUID())
            .build();

        when(followService.create(any()))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/follows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("GET /api/follows/summary - ⭕️ 요약 조회")
    void getFollowSummary_OK() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        FollowSummaryResponse response = FollowSummaryResponse.builder()
            .followerCount(10)
            .followingCount(5)
            .build();

        when(followService.getFollowSummary(any(), any()))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/follows/summary")
                .param("userId", userId.toString())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.followerCount").value(10))
            .andExpect(jsonPath("$.followingCount").value(5));
    }

    @Test
    @DisplayName("GET /api/follows/followings - ⭕️ 목록 조회")
    void getFollowings_OK() throws Exception {
        // given
        UUID followerId = UUID.randomUUID();

        List<FollowResponse> data = List.of(
            FollowResponse.builder().id(UUID.randomUUID()).build(),
            FollowResponse.builder().id(UUID.randomUUID()).build()
        );

        CursorResponse<FollowResponse> response =
            CursorResponse.fromList(
                data,
                "cursor",
                UUID.randomUUID(),
                true,
                "createdAt",
                SortDirection.DESCENDING
            );

        when(followService.getFollowings(any(), any(), any()))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/follows/followings")
                .param("followerId", followerId.toString())
                .param("limit", "2")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("GET /api/follows/followers - ⭕️ 목록 조회")
    void getFollowers_OK() throws Exception {
        // given
        UUID followeeId = UUID.randomUUID();

        List<FollowResponse> data = List.of(
            FollowResponse.builder().id(UUID.randomUUID()).build()
        );

        CursorResponse<FollowResponse> response =
            CursorResponse.fromList(
                data,
                "cursor",
                UUID.randomUUID(),
                false,
                "createdAt",
                SortDirection.DESCENDING
            );

        when(followService.getFollowers(any(), any(), any()))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/follows/followers")
                .param("followeeId", followeeId.toString())
                .param("limit", "1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("DELETE /api/follows/{followId} - ⭕️ 취소")
    void cancelFollow_OK() throws Exception {
        // given
        UUID followId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/follows/{id}", followId))
            .andExpect(status()
            .isNoContent());

        verify(followService).cancelFollow(followId);
    }

    @Test
    @DisplayName("DELETE /api/follows/{followId} - ❌ 실패")
    void cancelFollow_Fail() throws Exception {
        // given
        UUID followId = UUID.randomUUID();

        doThrow(new RuntimeException())
            .when(followService).cancelFollow(followId);

        // when & then
        mockMvc.perform(delete("/api/follows/{id}", followId))
            .andExpect(status().isInternalServerError());
    }
}