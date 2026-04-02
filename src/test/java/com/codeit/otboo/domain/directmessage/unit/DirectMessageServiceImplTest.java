package com.codeit.otboo.domain.directmessage.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.directmessage.mapper.DirectMessageMapper;
import com.codeit.otboo.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.otboo.domain.directmessage.service.DirectMessageServiceImpl;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.sse.event.SseEvent;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.websocket.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.websocket.event.DirectMessageCreatedEvent;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("🎯Unit Test >>> DirectMessageService")
@ExtendWith(MockitoExtension.class)
class DirectMessageServiceImplTest {

    @Mock
    private DirectMessageRepository directMessageRepository;

    @Mock
    private DirectMessageMapper directMessageMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DirectMessageServiceImpl directMessageService;

    @Test
    @DisplayName("⭕️ DM 생성 + 이벤트 발행")
    void create_success() {
        // given
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        User sender = User.builder()
            .email("sender@test.com")
            .password("pw")
            .build();

        User receiver = User.builder()
            .email("receiver@test.com")
            .password("pw")
            .build();

        ReflectionTestUtils.setField(sender, "id", senderId);
        ReflectionTestUtils.setField(receiver, "id", receiverId);

        given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
        given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));

        DirectMessageCreateRequest request =
            new DirectMessageCreateRequest(senderId, receiverId, "hello");

        DirectMessage dm = new DirectMessage(sender, receiver, "hello");

        given(directMessageRepository.save(any()))
            .willReturn(dm);

        DirectMessageResponse response =
            new DirectMessageResponse(
                UUID.randomUUID(),
                LocalDateTime.now(),
                new UserSummaryResponse(senderId, "sender", null),
                new UserSummaryResponse(receiverId, "receiver", null),
                "hello"
            );

        given(directMessageMapper.toDto(any(DirectMessage.class)))
            .willReturn(response);

        Notification savedNotification = Notification.builder()
            .title("t")
            .content("c")
            .level(NotificationLevel.INFO)
            .receiver(receiver)
            .build();

        ReflectionTestUtils.setField(savedNotification, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(savedNotification, "createdAt", LocalDateTime.now());

        given(notificationRepository.save(any()))
            .willReturn(savedNotification);

        // when
        directMessageService.create(request);

        // then
        verify(eventPublisher).publishEvent(any(DirectMessageCreatedEvent.class));
        verify(eventPublisher).publishEvent(any(SseEvent.class));
    }
}