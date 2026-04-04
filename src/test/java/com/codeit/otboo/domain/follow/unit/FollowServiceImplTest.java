package com.codeit.otboo.domain.follow.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.exception.follow.DuplicateFollowException;
import com.codeit.otboo.domain.follow.exception.follow.FollowNotFoundException;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.follow.service.FollowServiceImpl;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.sse.event.FollowSseEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@DisplayName("🎯Unit Test >>> FollowServiceImpl")
@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowMapper followMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FollowServiceImpl followService;

    private final TestFixture fixture = new TestFixture();

    /* ==========================
       Utility: Reflection으로 ID 세팅
       ========================== */
    private void setEntityId(Object entity, UUID id) {
        try {
            Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* ==========================
       Utility: 실제 User + Profile 객체 생성
       ========================== */
    private User createUserWithProfile(UUID id, String email, String password, String name) {
        User user = User.builder()
            .email(email)
            .password(password)
            .build();

        Profile profile = Profile.builder()
            .user(user)
            .name(name)
            .build();

        setEntityId(user, id); // reflection으로 id 주입
        return user;
    }

    private NotificationDto createNotificationDto(UUID receiverId) {
        return NotificationDto.builder()
            .id(UUID.randomUUID())
            .receiverId(receiverId)
            .createdAt(LocalDateTime.now())
            .title("알림")
            .content("")
            .level(NotificationLevel.INFO)
            .build();
    }

    @Test
    @DisplayName("팔로우 생성 성공 - 알림 생성 및 이벤트 발행")
    void create_success() {
        // UUID 준비
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        // --- 실제 User + Profile 엔티티 생성 ---
        User follower = User.builder()
            .email("follower@test.com")
            .password("pass")
            .build();
        Profile followerProfile = Profile.builder()
            .user(follower)
            .name("팔로워")
            .build();
        follower.setProfile(followerProfile);
        setEntityId(follower, followerId);

        User followee = User.builder()
            .email("followee@test.com")
            .password("pass")
            .build();
        Profile followeeProfile = Profile.builder()
            .user(followee)
            .name("팔로위")
            .build();
        followee.setProfile(followeeProfile);
        setEntityId(followee, followeeId);

        // --- 요청 객체 ---
        FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

        // --- repository stub ---
        given(userRepository.findById(any())).willAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (id.equals(followerId)) return Optional.of(follower);
            if (id.equals(followeeId)) return Optional.of(followee);
            return Optional.empty();
        });

        given(followRepository.findByFollowerIdAndFolloweeId(any(), any()))
            .willReturn(Optional.empty());

        Follow savedFollow = new Follow(follower, followee);
        given(followRepository.save(any(Follow.class))).willReturn(savedFollow);

        // --- FollowResponse stub ---
        given(followMapper.toDto(any(Follow.class)))
            .willReturn(FollowResponse.builder()
                .id(UUID.randomUUID())
                .build()
            );

        // --- when ---
        FollowResponse result = followService.create(request);

        // --- then ---
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();

        // follow 저장 검증
        verify(followRepository).save(any(Follow.class));

        // ❌ 서비스에서 안 부르므로 제거 (중요)
        // verify(notificationRepository).save(any(Notification.class));

        // ✅ 실제 발행 타입으로 변경 (핵심)
        verify(eventPublisher).publishEvent(any(FollowSseEvent.class));
    }

    @Test
    @DisplayName("팔로우 생성 실패 - 중복")
    void create_duplicate() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

        given(followRepository.findByFollowerIdAndFolloweeId(any(), any()))
            .willReturn(Optional.of(mock(Follow.class)));

        assertThatThrownBy(() -> followService.create(request))
            .isInstanceOf(DuplicateFollowException.class);
    }

    @Test
    @DisplayName("팔로우 생성 실패 - 유저 없음")
    void create_userNotFound() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

        given(followRepository.findByFollowerIdAndFolloweeId(any(), any()))
            .willReturn(Optional.empty());

        given(userRepository.findById(followerId))
            .willReturn(Optional.empty());
        given(userRepository.findById(followeeId))
            .willReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> followService.create(request))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("팔로우 취소 성공")
    void cancelFollow_success() {
        UUID id = UUID.randomUUID();
        Follow follow = mock(Follow.class);

        given(followRepository.findById(id)).willReturn(Optional.of(follow));

        followService.cancelFollow(id);

        verify(followRepository).delete(follow);
    }

    @Test
    @DisplayName("팔로우 취소 실패 - 없음")
    void cancelFollow_fail() {
        UUID id = UUID.randomUUID();
        given(followRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> followService.cancelFollow(id))
            .isInstanceOf(FollowNotFoundException.class);
    }

    @Test
    @DisplayName("팔로잉 조회 - hasNext true")
    void getFollowings_hasNext() {
        CursorRequest request = new CursorRequest(null, null, 2);
        UUID userId = UUID.randomUUID();

        List<FollowDto> results = List.of(
            fixture.followDto(),
            fixture.followDto(),
            fixture.followDto()
        );

        given(followRepository.findAllFollowings(any(), any(), any(), any(), any()))
            .willReturn(results);

        given(followMapper.toDto(any(FollowDto.class)))
            .willReturn(FollowResponse.builder().id(UUID.randomUUID()).build());

        CursorResponse<FollowResponse> result =
            followService.getFollowings(userId, null, request);

        assertThat(result.data().size()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }
}