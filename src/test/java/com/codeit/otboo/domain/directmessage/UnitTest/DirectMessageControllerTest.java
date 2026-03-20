package com.codeit.otboo.domain.directmessage.UnitTest;

import com.codeit.otboo.domain.directmessage.controller.DirectMessageController;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.service.DirectMessageService;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("🎯Unit Test> DirectMessageController")
@WebMvcTest(
        controllers = DirectMessageController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class DirectMessageControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DirectMessageService directMessageService;

    private final TestFixture fixture = new TestFixture();

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("🎯 GET /api/direct-messages - ⭕️ 정상 조회")
    void getDirectMessages_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        UserSummaryResponse sender = new UserSummaryResponse(
            UUID.randomUUID(),
            "sender",
            "img"
        );

        UserSummaryResponse receiver = new UserSummaryResponse(
            userId,
            "receiver",
            "img"
        );

        DirectMessageResponse response1 = new DirectMessageResponse(
            UUID.randomUUID(),
            now.minusSeconds(1),
            sender,
            receiver,
            "msg1"
        );

        DirectMessageResponse response2 = new DirectMessageResponse(
            UUID.randomUUID(),
            now.minusSeconds(2),
            sender,
            receiver,
            "msg2"
        );

        Slice<DirectMessageResponse> slice =
            new SliceImpl<>(
                List.of(response1, response2),
                PageRequest.of(0, 2),
                false
            );

        CursorResponse<DirectMessageResponse> mockResponse =
            CursorResponse.fromSlice(
                slice,
                null,
                null,
                "createdAt",
                SortDirection.DESCENDING
            );

        given(directMessageService.getDirectMessages(any(), any()))
            .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/direct-messages")
                .param("userId", userId.toString())
                .param("limit", "2")
                .param("cursor", "")
                .param("idAfter", "")
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("🎯 GET /api/direct-messages - ❌ DM 목록 조회 실패")
    void getDirectMessages_fail() {

    }
}