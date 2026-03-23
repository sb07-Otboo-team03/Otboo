package com.codeit.otboo.domain.notification.Unit;

import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.domain.notification.controller.NotificationController;
import com.codeit.otboo.domain.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("🎯Unit Test> NotificationController")
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NotificationService notificationService;

    private final TestFixture fixture = new TestFixture();

    @BeforeEach
    void setUp() {
    }

//    @Test
    @DisplayName("🎯 GET /api/notifications - ⭕️ 정상 조회")
    void getDirectMessages_OK() {
    }

//    @Test
    @DisplayName("🎯 GET /api/notifications - ❌ 실패")
    void getDirectMessages_Fail() {
    }

//    @Test
    @DisplayName("🎯 GET /api/notifications/{notificationId} - ⭕️ 정상 조회")
    void getDirectMessages_byId_OK() {
    }

//    @Test
    @DisplayName("🎯 GET /api/notifications/{notificationId} - ❌ 실패")
    void getDirectMessages_byId_Fail() {
    }
}