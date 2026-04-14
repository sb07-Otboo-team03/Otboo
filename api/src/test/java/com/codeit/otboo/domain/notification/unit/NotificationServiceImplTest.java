package com.codeit.otboo.domain.notification.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.exception.notification.NotificationNotFoundException;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.notification.service.NotificationServiceImpl;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@DisplayName("🎯Unit Test >>> NotificationServiceImpl")
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private final TestFixture fixture = new TestFixture();

    @Test
    @DisplayName("알림 조회 - hasNext = true (커서 페이징 정상 동작)")
    void getNotifications_hasNext_true() {
        // given
        int limit = 2;
        CursorRequest request = new CursorRequest(null, null, limit);
        UUID authPrincipalId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        NotificationDto dto1 = fixture.notificationDtoBuilder().createdAt(now.minusSeconds(1)).build();
        NotificationDto dto2 = fixture.notificationDtoBuilder().createdAt(now.minusSeconds(2)).build();
        NotificationDto dto3 = fixture.notificationDtoBuilder().createdAt(now.minusSeconds(3)).build();

        List<NotificationDto> mockResults = List.of(dto1, dto2, dto3);

        when(notificationRepository.findAllByReceiverId(
            eq(authPrincipalId),
            nullable(LocalDateTime.class),
            nullable(UUID.class),
            any(Pageable.class))
        ).thenReturn(mockResults);

        when(notificationMapper.toDto(any(NotificationDto.class)))
            .thenAnswer(invocation -> {
                NotificationDto dto = invocation.getArgument(0);
                return NotificationResponse.builder().id(dto.id()).build();
            });

        // when
        CursorResponse<NotificationResponse> result =
            notificationService.getNotifications(authPrincipalId, request);

        // then
        assertThat(result.data()).hasSize(limit);
        assertThat(result.hasNext()).isTrue();

        NotificationDto last = mockResults.get(limit - 1);
        assertThat(result.nextCursor()).isEqualTo(last.createdAt().toString());
        assertThat(result.nextIdAfter()).isEqualTo(last.id());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findAllByReceiverId(
            eq(authPrincipalId),
            nullable(LocalDateTime.class),
            nullable(UUID.class),
            captor.capture()
        );
        assertThat(captor.getValue().getPageSize()).isEqualTo(limit + 1);
    }

    @Test
    @DisplayName("알림 조회 - 마지막 페이지 (hasNext = false)")
    void getNotifications_hasNext_false() {
        int limit = 3;
        CursorRequest request = new CursorRequest(null, null, limit);
        UUID authPrincipalId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();
        NotificationDto dto1 = fixture.notificationDtoBuilder().createdAt(now.minusSeconds(1)).build();
        NotificationDto dto2 = fixture.notificationDtoBuilder().createdAt(now.minusSeconds(2)).build();
        List<NotificationDto> mockResults = List.of(dto1, dto2);

        when(notificationRepository.findAllByReceiverId(
            eq(authPrincipalId),
            nullable(LocalDateTime.class),
            nullable(UUID.class),
            any(Pageable.class))
        ).thenReturn(mockResults);

        when(notificationMapper.toDto(any(NotificationDto.class)))
            .thenAnswer(invocation -> {
                NotificationDto dto = invocation.getArgument(0);
                return NotificationResponse.builder().id(dto.id()).build();
            });

        // when
        CursorResponse<NotificationResponse> result =
            notificationService.getNotifications(authPrincipalId, request);

        // then
        assertThat(result.data()).hasSize(2);
        assertThat(result.hasNext()).isFalse();

        NotificationDto last = mockResults.get(mockResults.size() - 1);
        assertThat(LocalDateTime.parse(result.nextCursor())).isEqualTo(last.createdAt());
        assertThat(result.nextIdAfter()).isEqualTo(last.id());
    }

    @Test
    @DisplayName("알림 삭제 성공 ⭕️")
    void deleteNotification_success() {
        UUID notificationId = UUID.randomUUID();
        UUID authPrincipalId = UUID.randomUUID();

        Notification notification = mock(Notification.class);
        when(notificationRepository.findByIdAndReceiverId(notificationId, authPrincipalId))
            .thenReturn(Optional.of(notification));

        // when
        notificationService.deleteNotification(authPrincipalId, notificationId);

        // then
        verify(notificationRepository).delete(notification);
    }

    @Test
    @DisplayName("알림 삭제 실패 ❌ - 존재하지 않음")
    void deleteNotification_notFound() {
        UUID notificationId = UUID.randomUUID();
        UUID authPrincipalId = UUID.randomUUID();

        when(notificationRepository.findByIdAndReceiverId(notificationId, authPrincipalId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            notificationService.deleteNotification(authPrincipalId, notificationId)
        ).isInstanceOf(NotificationNotFoundException.class);
    }
}