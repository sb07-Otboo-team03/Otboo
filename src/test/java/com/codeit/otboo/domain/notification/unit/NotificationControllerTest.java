package com.codeit.otboo.domain.notification.unit;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.otboo.domain.notification.controller.NotificationController;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificationController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class
        )})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    private UUID userId;
    private UUID notificationId;
    private OtbooUserDetails userDetails;
    private NotificationResponse dto;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        UserResponse userDto = UserResponse.builder()
            .id(userId)
            .role(Role.USER)
            .build();

        userDetails = new OtbooUserDetails(userDto, "otboo123");

        dto = NotificationResponse.builder()
            .id(notificationId)
            .createdAt(LocalDateTime.now())
            .receiverId(userId)
            .title("title")
            .content("content")
            .level(NotificationLevel.INFO)
            .build();
    }

    @Nested
    @DisplayName("알림 목록 조회")
    class GetNotifications {

        @Test
        @DisplayName("알림 목록 조회 성공")
        void getNotifications_Success() throws Exception {
            // given
            List<NotificationResponse> list = List.of(dto);

            CursorResponse<NotificationResponse> response =
                new CursorResponse<>(
                    list,
                    null,
                    null,
                    false,
                    1L,
                    "createdAt",
                    SortDirection.DESCENDING
                );

            given(notificationService.getNotifications(eq(userId), any()))
                .willReturn(response);

            // when & then
//            mockMvc.perform(get("/api/notifications")
//                    .with(user(userDetails))
//                )
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data[0].id").value(notificationId.toString()))
//                .andExpect(jsonPath("$.data[0].receiverId").value(userId.toString()))
//                .andExpect(jsonPath("$.data[0].title").value("title"))
//                .andExpect(jsonPath("$.data[0].content").value("content"));

            mockMvc.perform(get("/api/notifications")
                    .param("limit", "10")   // ⭐ 이거 추가
                    .with(user(userDetails))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(notificationId.toString()))
                .andExpect(jsonPath("$.data[0].receiverId").value(userId.toString()))
                .andExpect(jsonPath("$.data[0].title").value("title"))
                .andExpect(jsonPath("$.data[0].content").value("content"));

            // 🔥 핵심 검증
            verify(notificationService).getNotifications(eq(userId), any());
        }
    }

    @Nested
    @DisplayName("알림 삭제")
    class DeleteNotification {

        @Test
        @DisplayName("알림 삭제 성공")
        void deleteNotification_Success() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
                    .with(csrf())
                    .with(user(userDetails))
                )
                .andExpect(status().isNoContent());

            // 🔥 핵심 검증
            verify(notificationService)
                .deleteNotification(eq(userId), eq(notificationId));
        }
    }
}