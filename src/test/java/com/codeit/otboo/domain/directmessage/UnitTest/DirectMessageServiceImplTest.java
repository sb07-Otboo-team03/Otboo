package com.codeit.otboo.domain.directmessage.UnitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.otboo.domain.directmessage.service.DirectMessageServiceImpl;
import com.codeit.otboo.domain.util.TestFixture;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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
    private User user_II;
    private DirectMessage directMessage_I;
    private DirectMessage directMessage_II;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        user_I = fixture.mockUserWithProfile(now);
        user_II = fixture.mockUserWithProfile(now.minusSeconds(1));
        directMessage_I = fixture.mockDirectMessage(user_I, user_II, "msg1", now.minusSeconds(2));
        directMessage_II = fixture.mockDirectMessage(user_I, user_II, "msg2", now.minusSeconds(3));
    }

    @Test
    @DisplayName("🎯 getDirectMessages - 정상 조회")
    void getDirectMessages_case_I() {
        // given
        UUID userId = fixture.getRandomID();

        CursorRequest cursorRequest = new CursorRequest(
            null,
            null,
            2
        );

        Pageable pageable = PageRequest.of(0, cursorRequest.limit() + 1);

        Slice<DirectMessage> slice = new SliceImpl<>(
            List.of(directMessage_I, directMessage_II),
            pageable,
            false // hasNext
        );

        given(directMessageRepository.findDirectMessages(
            eq(userId),
            isNull(),
            isNull(),
            eq(pageable)
        )).willReturn(slice);

        // when
        CursorResponse<DirectMessageResponse> result =
            directMessageService.getDirectMessages(userId, cursorRequest);

        // then
        assertThat(result.data()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
    }
}