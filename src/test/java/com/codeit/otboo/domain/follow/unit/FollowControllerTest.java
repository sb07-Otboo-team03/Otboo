package com.codeit.otboo.domain.follow.unit;

import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.domain.follow.controller.FollowController;
import com.codeit.otboo.domain.follow.service.FollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("🎯Unit Test> FollowController")
@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc(addFilters = false)
class FollowControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FollowService followService;

    private final TestFixture fixture = new TestFixture();

    @BeforeEach
    void setUp() {
    }

//    @Test
    void follow() {
    }
}