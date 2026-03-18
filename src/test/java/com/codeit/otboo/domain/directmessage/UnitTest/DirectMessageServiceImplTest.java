package com.codeit.otboo.domain.directmessage.UnitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageDto;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.otboo.domain.directmessage.service.DirectMessageServiceImpl;
import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@DisplayName("🎯Unit Test >>>> DirectMessageService")
@ExtendWith(MockitoExtension.class)
class DirectMessageServiceImplTest {
    @Mock
    DirectMessageRepository directMessageRepository;

    @InjectMocks
    DirectMessageServiceImpl directMessageService;

    private final int limit = 50;
    private final int offset = 0;
    private final String sort = "createdAt";
    private final String order = "desc";

    private final TestFixture fixture = new TestFixture();
    private User user_I;
    private User user_V;
    private User user_III;
    private DirectMessage directMessage_I;
    private DirectMessage directMessage_III;
    private DirectMessageResponse directMessageResponse_I;
    private DirectMessageResponse directMessageResponse_II;

    private DirectMessageDto directMessageDto_I;
    private DirectMessageDto directMessageDto_III;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        user_I = fixture.mockUserWithProfile(now);
        user_V = fixture.mockUserWithProfile(now.minusSeconds(1));
        user_III = fixture.mockUserWithProfile(now.minusSeconds(2));

        directMessage_I = fixture.mockDirectMessage(user_I, user_V, "msg1", now.minusSeconds(3));
        directMessage_III = fixture.mockDirectMessage(user_III, user_V, "msg3", now.minusSeconds(4));

        directMessageResponse_I = fixture.mockDirectMessageResponse(directMessage_I, user_I, user_V);
        directMessageResponse_II = fixture.mockDirectMessageResponse(directMessage_III, user_III, user_V);

        directMessageDto_I = fixture.mockDirectMessageDto(directMessage_I, user_I, user_V, now.minusSeconds(5));
        directMessageDto_III = fixture.mockDirectMessageDto(directMessage_III, user_III, user_V, now.minusSeconds(3));
    }

    @Test
    @DisplayName("🎯 getDirectMessages - ⭕️ 정상 조회")
    void getDirectMessages_OK() {
        UUID userId = fixture.getRandomID();
        CursorRequest cursorRequest = new CursorRequest(null, null, 2);

        List<DirectMessageDto> responses = List.of(directMessageDto_I, directMessageDto_III);

        given(directMessageRepository.findDirectMessageDtos(
            eq(userId),
            isNull(),
            isNull(),
            any(Pageable.class)
        )).willReturn(responses);

        CursorResponse<DirectMessageResponse> result =
            directMessageService.getDirectMessages(userId, cursorRequest);

        assertThat(result.data()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("🎯 getDirectMessages - ❌ 빈 리스트 조회")
    void getDirectMessages_Empty() {
        UUID userId = fixture.getRandomID();
        CursorRequest cursorRequest = new CursorRequest(null, null, 2);

        given(directMessageRepository.findDirectMessageDtos(
            eq(userId),
            isNull(),
            isNull(),
            any(Pageable.class)
        )).willReturn(Collections.emptyList());

        CursorResponse<DirectMessageResponse> result =
            directMessageService.getDirectMessages(userId, cursorRequest);

        assertThat(result.data()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }
}