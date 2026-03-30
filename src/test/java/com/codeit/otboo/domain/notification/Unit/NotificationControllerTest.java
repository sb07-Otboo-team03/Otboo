package com.codeit.otboo.domain.notification.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.domain.notification.controller.NotificationController;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.exception.notification.DuplicateNotificationException;
import com.codeit.otboo.domain.notification.exception.notification.NotificationNotFoundException;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.security.jwt.JwtProvider;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("🎯Unit Test> NotificationController")
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NotificationService notificationService;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    JwtProvider jwtProvider;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private final TestFixture fixture = new TestFixture();

    @Test
    @DisplayName("🎯 GET /api/notifications - ⭕️ 정상 조회")
    void getNotifications_OK() throws Exception {
        // given
        List<NotificationResponse> data = List.of(
            NotificationResponse.builder().id(UUID.randomUUID()).build(),
            NotificationResponse.builder().id(UUID.randomUUID()).build()
        );

        CursorResponse<NotificationResponse> response =
            CursorResponse.fromList(
                data,
                "2026-03-30T10:00:00",
                UUID.randomUUID(),
                true,
                "createdAt",
                SortDirection.DESCENDING
            );

        when(notificationService.getNotifications(any()))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/notifications")
                .param("limit", "2")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.nextCursor").exists());

    }

    @Test
    @DisplayName("🎯 GET /api/notifications - ❌ 서비스 예외")
    void getNotifications_Fail() throws Exception {
        // given
        when(notificationService.getNotifications(any()))
            .thenThrow(new RuntimeException("error"));

        // when & then
        mockMvc.perform(get("/api/notifications")
                .param("limit", "2")
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("🎯 DELETE /api/notifications/{notificationId} - ⭕️ 정상 삭제")
    void deleteNotification_OK() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/notifications/{id}", id))
            .andExpect(status().isNoContent());

        verify(notificationService).deleteNotification(id);
    }

    @Test
    @DisplayName("🎯 DELETE /api/notifications/{notificationId} - ❌ 존재하지 않음")
    void deleteNotification_Fail() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        doThrow(new NotificationNotFoundException(id))
            .when(notificationService).deleteNotification(id);

        // when & then
        mockMvc.perform(delete("/api/notifications/{id}", id))
            .andExpect(status().isNotFound());
    }
}