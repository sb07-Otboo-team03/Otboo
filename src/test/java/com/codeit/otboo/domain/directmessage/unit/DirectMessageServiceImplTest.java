package com.codeit.otboo.domain.directmessage.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageDto;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.mapper.DirectMessageMapper;
import com.codeit.otboo.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.otboo.domain.directmessage.service.DirectMessageServiceImpl;
import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@DisplayName("🎯Unit Test >>> DirectMessageService")
@ExtendWith(MockitoExtension.class)
class DirectMessageServiceImplTest {

    @Mock
    DirectMessageRepository directMessageRepository;

    @Mock
    DirectMessageMapper directMessageMapper;

    @InjectMocks
    DirectMessageServiceImpl directMessageService;

    private final TestFixture fixture = new TestFixture();

    private DirectMessageDto dto1;
    private DirectMessageDto dto2;
    private DirectMessageDto dto3;

    private DirectMessageResponse res1;
    private DirectMessageResponse res2;
    private DirectMessageResponse res3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        dto1 = fixture.mockDirectMessageDtoWithTime(now.minusSeconds(1));
        dto2 = fixture.mockDirectMessageDtoWithTime(now.minusSeconds(2));
        dto3 = fixture.mockDirectMessageDtoWithTime(now.minusSeconds(3));

        res1 = fixture.mockDirectMessageResponse(now.minusSeconds(4));
        res2 = fixture.mockDirectMessageResponse(now.minusSeconds(5));
        res3 = fixture.mockDirectMessageResponse(now.minusSeconds(6));
    }

    @Test
    @DisplayName("⭕️ 정상 조회 - hasNext = false")
    void getDirectMessages_OK_noNext() {
        // given
        UUID userId = fixture.getRandomID();
        CursorRequest request = new CursorRequest(null, null, 2);

        given(directMessageRepository.findDirectMessageDtos(
            eq(userId),
            isNull(),
            isNull(),
            any(Pageable.class)
        )).willReturn(List.of(dto1, dto2));

        given(directMessageMapper.toDto(dto1)).willReturn(res1);
        given(directMessageMapper.toDto(dto2)).willReturn(res2);

        // when
        CursorResponse<DirectMessageResponse> result =
            directMessageService.getDirectMessages(userId, request);

        // then
        assertThat(result.data()).hasSize(2);
        assertThat(result.data()).containsExactly(res1, res2);

        assertThat(result.hasNext()).isFalse();

        assertThat(result.nextCursor())
            .isEqualTo(dto2.createdAt().toString());

        assertThat(result.nextIdAfter())
            .isEqualTo(dto2.id());
    }

    @Test
    @DisplayName("⭕️ 다음 페이지 존재(잘렸는지 확인) - hasNext = true")
    void getDirectMessages_hasNext() {
        // given
        UUID userId = fixture.getRandomID();
        CursorRequest request = new CursorRequest(null, null, 2);

        given(directMessageRepository.findDirectMessageDtos(
            eq(userId),
            isNull(),
            isNull(),
            any(Pageable.class)
        )).willReturn(List.of(dto1, dto2, dto3)); // limit + 1

        given(directMessageMapper.toDto(dto1)).willReturn(res1);
        given(directMessageMapper.toDto(dto2)).willReturn(res2);

        // when
        CursorResponse<DirectMessageResponse> result =
            directMessageService.getDirectMessages(userId, request);

        // then
        assertThat(result.data()).hasSize(2);
        assertThat(result.data()).containsExactly(res1, res2);

        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor())
            .isEqualTo(dto2.createdAt().toString());

        assertThat(result.nextIdAfter())
            .isEqualTo(dto2.id());
    }

    @Test
    @DisplayName("⭕️ 빈 리스트 조회")
    void getDirectMessages_empty() {
        UUID userId = fixture.getRandomID();
        CursorRequest request = new CursorRequest(null, null, 2);

        given(directMessageRepository.findDirectMessageDtos(
            eq(userId),
            isNull(),
            isNull(),
            any(Pageable.class)
        )).willReturn(Collections.emptyList());

        CursorResponse<DirectMessageResponse> result =
            directMessageService.getDirectMessages(userId, request);

        assertThat(result.data()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.nextIdAfter()).isNull();
    }

    @Test
    @DisplayName("⭕️ cursor 기반 조회 - repository 파라미터 검증")
    void getDirectMessages_withCursor() {
        UUID userId = fixture.getRandomID();

        LocalDateTime cursorTime = LocalDateTime.now().minusMinutes(1);
        UUID idAfter = fixture.getRandomID();

        CursorRequest request =
            new CursorRequest(cursorTime.toString(), idAfter, 2);

        given(directMessageRepository.findDirectMessageDtos(
            eq(userId),
            eq(cursorTime),
            eq(idAfter),
            any(Pageable.class)
        )).willReturn(List.of(dto1, dto2));

        given(directMessageMapper.toDto(dto1)).willReturn(res1);
        given(directMessageMapper.toDto(dto2)).willReturn(res2);

        CursorResponse<DirectMessageResponse> result =
            directMessageService.getDirectMessages(userId, request);

        assertThat(result.data()).hasSize(2);

        verify(directMessageRepository).findDirectMessageDtos(
            eq(userId),
            eq(cursorTime),
            eq(idAfter),
            any(Pageable.class)
        );
    }

    @Test
    @DisplayName("⭕️ Pageable limit + 1 검증 ")
    void getDirectMessages_pageableValidation() {
        UUID userId = fixture.getRandomID();
        CursorRequest request = new CursorRequest(null, null, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        given(directMessageRepository.findDirectMessageDtos(
            eq(userId),
            isNull(),
            isNull(),
            captor.capture()
        )).willReturn(List.of());

        directMessageService.getDirectMessages(userId, request);

        Pageable pageable = captor.getValue();

        assertThat(pageable.getPageSize()).isEqualTo(6);
    }
}