package com.codeit.otboo.domain.like.controller;

import com.codeit.otboo.domain.like.service.LikeService;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LikeController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )})
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LikeService likeService;

    private UUID feedId;
    private OtbooUserDetails userDetails;

    @BeforeEach
    void setUp() {
        // given
        feedId = UUID.randomUUID();
        UserResponse userDto = UserResponse.builder().id(UUID.randomUUID()).role(Role.USER).build();
        userDetails = new OtbooUserDetails(userDto, "otboo123");
    }

    @Test
    @DisplayName("좋아요를 누를 수 있다.")
    void likeFeed_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/like", feedId)
                        .with(csrf())
                        .with(user(userDetails))
                )
                .andExpect(status().isNoContent());
        verify(likeService).feedLike(eq(feedId), any());
    }

    @Test
    @WithMockUser(username = "otboo@a.a", password = "otboo123")
    @DisplayName("좋아요 취소")
    void unlikeFeed_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/feeds/{feedId}/like", feedId)
                        .with(csrf())
                        .with(user(userDetails))
                )
                .andExpect(status().isNoContent());
        verify(likeService).feedUnlike(eq(feedId), any());

    }
}