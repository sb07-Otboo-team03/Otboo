package com.codeit.otboo.domain.follow.unit;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.exception.follow.DuplicateFollowException;
import com.codeit.otboo.domain.follow.exception.follow.FollowNotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.*;
import org.springframework.context.ApplicationEventPublisher;
import com.codeit.otboo.domain.follow.service.FollowServiceImpl;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.domain.directmessage.util.TestFixture;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @DisplayName("팔로우 생성 성공")
    FollowCreateRequest createFollow() {
        // given
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        FollowCreateRequest request =
            new FollowCreateRequest(followerId, followeeId);

        LocalDateTime now = LocalDateTime.now();

        User follower = fixture.mockUserWithProfile(now.minusSeconds(1));
        User followee = fixture.mockUserWithProfile(now.minusSeconds(2));

        Follow follow = new Follow(follower, followee);

        return request;
    }

    private FollowCreateRequest createRequest() {
        return new FollowCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID()
        );
    }

    @Test
    void createFollow_fail() {
        FollowCreateRequest request = createFollow();

        // given
        when(followRepository.findByFollowerIdAndFolloweeId(any(), any()))
            .thenReturn(Optional.of(mock(Follow.class)));

        // when & then
        assertThatThrownBy(() -> followService.createFollow(request))
            .isInstanceOf(DuplicateFollowException.class);
    }

    @Test
    void createFollow_success() {
        FollowCreateRequest request = createRequest();

        User user = fixture.mockUserWithProfile(LocalDateTime.now());

        when(followRepository.findByFollowerIdAndFolloweeId(any(), any()))
            .thenReturn(Optional.empty());

        when(userRepository.findById(any()))
            .thenReturn(Optional.of(user));

        when(followRepository.save(any()))
            .thenReturn(mock(Follow.class));

        when(followMapper.toDto(any(Follow.class)))
            .thenReturn(FollowResponse.builder()
                .id(UUID.randomUUID())
                .build());

        FollowResponse result = followService.createFollow(request);

        assertThat(result).isNotNull();
    }


    @Test
    @DisplayName("팔로우 생성 실패 - 중복")
    void createFollow_duplicate() {
        // given
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        FollowCreateRequest request =
            new FollowCreateRequest(followerId, followeeId);

        when(followRepository.findByFollowerIdAndFolloweeId(any(), any()))
            .thenReturn(Optional.of(mock(Follow.class)));

        // when & then
        assertThatThrownBy(() -> followService.createFollow(request))
            .isInstanceOf(DuplicateFollowException.class);
    }

    @Test
    @DisplayName("팔로우 생성 실패 - 유저 없음")
    void createFollow_userNotFound() {
        // given
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        FollowCreateRequest request =
            new FollowCreateRequest(followerId, followeeId);

        when(followRepository.findByFollowerIdAndFolloweeId(any(), any()))
            .thenReturn(Optional.empty());

        // ✅ follower 조회 실패 상황
        when(userRepository.findById(followerId))
            .thenReturn(Optional.empty());

        // ✅ followee도 호출되므로 stub 필요 (strict 방지)
        when(userRepository.findById(followeeId))
            .thenReturn(Optional.of(mock(User.class)));

        // when & then
        assertThatThrownBy(() -> followService.createFollow(request))
            .isInstanceOf(UserNotFoundException.class); // ✅ 수정
    }

    @Test
    @DisplayName("팔로우 취소 성공")
    void cancelFollow_success() {
        // given
        UUID id = UUID.randomUUID();
        Follow follow = mock(Follow.class);

        when(followRepository.findById(id))
            .thenReturn(Optional.of(follow));

        // when
        followService.cancelFollow(id);

        // then
        verify(followRepository).delete(follow);
    }

    @Test
    @DisplayName("팔로우 취소 실패 - 없음")
    void cancelFollow_fail() {
        // given
        UUID id = UUID.randomUUID();

        when(followRepository.findById(id))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> followService.cancelFollow(id))
            .isInstanceOf(FollowNotFoundException.class);
    }

    @Test
    @DisplayName("팔로잉 조회 - hasNext true")
    void getFollowings_hasNext() {
        // given
        CursorRequest request = new CursorRequest(null, null, 2);
        UUID userId = UUID.randomUUID();

        List<FollowDto> results = List.of(
            fixture.followDto(),
            fixture.followDto(),
            fixture.followDto()
        );

        when(followRepository.findAllFollowings(any(), any(), any(), any(), any()))
            .thenReturn(results);

        when(followMapper.toDto(any(FollowDto.class)))
            .thenReturn(
                FollowResponse.builder()
                    .id(UUID.randomUUID())
                    .build()
            );

        // when
        CursorResponse<FollowResponse> result =
            followService.getFollowings(userId, null, request);

        // then
        assertThat(result.data().size()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }
}