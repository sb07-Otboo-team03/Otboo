//package com.codeit.otboo.domain.follow.unit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//
//import com.codeit.otboo.domain.BaseUpdatableEntity;
//import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
//import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
//import com.codeit.otboo.domain.directmessage.mapper.DirectMessageMapper;
//import com.codeit.otboo.domain.directmessage.repository.DirectMessageRepository;
//import com.codeit.otboo.domain.directmessage.service.DirectMessageServiceImpl;
//import com.codeit.otboo.domain.notification.dto.NotificationDto;
//import com.codeit.otboo.domain.notification.dto.NotificationLevel;
//import com.codeit.otboo.domain.notification.entity.Notification;
//import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
//import com.codeit.otboo.domain.notification.repository.NotificationRepository;
//import com.codeit.otboo.domain.profile.entity.Profile;
//import com.codeit.otboo.domain.sse.event.SseEvent;
//import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
//import com.codeit.otboo.domain.user.entity.User;
//import com.codeit.otboo.domain.user.repository.UserRepository;
//import com.codeit.otboo.domain.websocket.dto.DirectMessageCreateRequest;
//import java.lang.reflect.Field;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.ApplicationEventPublisher;
//
//@ExtendWith(MockitoExtension.class)
//class DirectMessageServiceImplTest {
//
//    @Mock
//    private DirectMessageRepository directMessageRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private DirectMessageMapper directMessageMapper; // ← mock 처리
//
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//
//    @Mock
//    private NotificationRepository notificationRepository;
//
//    @Mock
//    private NotificationMapper notificationMapper; // ← mock 처리
//
//    @InjectMocks
//    private DirectMessageServiceImpl directMessageService;
//
//    // Reflection으로 Entity ID 세팅
//    private void setEntityId(Object entity, UUID id) {
//        try {
//            Field field = BaseUpdatableEntity.class.getDeclaredField("id");
//            field.setAccessible(true);
//            field.set(entity, id);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private User createUser(UUID id, String email, String name) {
//        User user = User.builder()
//            .email(email)
//            .password("pass")
//            .build();
//
//        Profile profile = Profile.builder()
//            .user(user)
//            .name(name)
//            .build();
//
//        user.setProfile(profile);
//        setEntityId(user, id);
//        return user;
//    }
//
//    @Test
//    @DisplayName("⭕️ DM 생성 + 알림 + 이벤트 발행")
//    void createDirectMessage_success() {
//        UUID senderId = UUID.randomUUID();
//        UUID receiverId = UUID.randomUUID();
//
//        User sender = createUser(senderId, "sender@test.com", "Sender");
//        User receiver = createUser(receiverId, "receiver@test.com", "Receiver");
//
//        DirectMessageCreateRequest request =
//            new DirectMessageCreateRequest(senderId, receiverId, "Hello!");
//
//        given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
//        given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
//
//        DirectMessage savedMessage = new DirectMessage(sender, receiver, "Hello!");
//        setEntityId(savedMessage, UUID.randomUUID());
//
//        given(directMessageRepository.save(any(DirectMessage.class))).willReturn(savedMessage);
//
//        // Mapper stub
//        UserSummaryResponse senderSummary = new UserSummaryResponse(
//            sender.getId(),
//            sender.getProfile().getName(),
//            sender.getEmail()
//        );
//
//        UserSummaryResponse receiverSummary = new UserSummaryResponse(
//            receiver.getId(),
//            receiver.getProfile().getName(),
//            receiver.getEmail()
//        );
//
//        DirectMessageResponse response = new DirectMessageResponse(
//            savedMessage.getId(),
//            savedMessage.getCreatedAt(),
//            senderSummary,
//            receiverSummary,
//            savedMessage.getContent()
//        );
//        given(directMessageMapper.toDto(savedMessage)).willReturn(response);
//
//        Notification notification = Notification.builder()
//            .title("[DM]" + response.sender().name())
//            .content(response.content())
//            .level(NotificationLevel.INFO)
//            .receiver(receiver)
//            .build();
//        setEntityId(notification, UUID.randomUUID());
//
//        NotificationDto notificationDto = NotificationDto.builder()
//            .id(notification.getId())
//            .receiverId(receiver.getId())
//            .title(notification.getTitle())
//            .content(notification.getContent())
//            .level(notification.getLevel())
//            .createdAt(notification.getCreatedAt())
//            .build();
//
//        given(notificationRepository.save(any(Notification.class)))
//            .willReturn(notification);
//
//        given(notificationMapper.toEventDto(notification)).willReturn(notificationDto);
//
//        // --- 서비스 호출 ---
//        DirectMessageResponse result = directMessageService.create(request);
//
//        // --- 검증 ---
//        assertThat(result).isNotNull();
//        assertThat(result.content()).isEqualTo("Hello!");
//
//        verify(directMessageRepository).save(any(DirectMessage.class));
//        verify(notificationRepository).save(any(Notification.class));
//        verify(eventPublisher).publishEvent(any(SseEvent.class));
//    }
//}