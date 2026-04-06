package com.codeit.otboo.domain.follow.service;

import static com.codeit.otboo.domain.user.entity.QUser.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @InjectMocks
    private FollowServiceImpl followService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowMapper followMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UUID followerId;
    private UUID followeeId;

    private User follower;
    private User followee;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        followeeId = UUID.randomUUID();

        follower = User.builder().build();
        ReflectionTestUtils.setField(follower, "id", followerId);
        new Profile(follower, "follower");

        followee = User.builder().build();
        ReflectionTestUtils.setField(followee, "id", followeeId);
        new Profile(followee, "followee");
    }

    @Nested
    @DisplayName("팔로우 생성")
    class CreateFollow {

        @Test
        @DisplayName("팔로우 생성 성공")
        void create_success() {
            // given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

            Follow follow = new Follow(follower, followee);
            Follow savedFollow = follow;

            FollowResponse response = FollowResponse.builder()
                .id(UUID.randomUUID())
                .build();

            given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(userRepository.findById(followeeId)).willReturn(Optional.of(followee));
            given(followRepository.save(any())).willReturn(savedFollow);
            given(followMapper.toDto(savedFollow)).willReturn(response);

            // when
            FollowResponse result = followService.create(request);

            // then
            assertThat(result).isEqualTo(response);

            verify(followRepository).save(any(Follow.class));
            verify(eventPublisher).publishEvent(any()); // 🔥 이벤트 발행 검증
        }

        @Test
        @DisplayName("팔로우 생성 실패 - 팔로위 없음")
        void create_fail_followee_not_found() {
            // given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

            given(userRepository.findById(followeeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.create(request))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("팔로우 요약 조회")
    class FollowSummary {

        @Test
        @DisplayName("팔로우 안 한 상태")
        void getFollowSummary_notFollowing() {
            // given
            UUID myId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();

            OtbooUserDetails userDetails = mock(OtbooUserDetails.class);
            UserResponse userResponse = UserResponse.builder().id(myId).build();

            given(userDetails.getUserResponse()).willReturn(userResponse);
            given(userRepository.findById(followeeId)).willReturn(Optional.of(mock(User.class)));

            given(followRepository.countByFollowerId(followeeId)).willReturn(10);
            given(followRepository.countByFolloweeId(followeeId)).willReturn(5);

            // 팔로우 관계 없음
            given(followRepository.findByFollowerIdAndFolloweeId(myId, followeeId))
                .willReturn(Optional.empty());

            // when
            FollowSummaryResponse result =
                followService.getFollowSummary(followeeId, userDetails);

            // then
            assertThat(result.followeeId()).isEqualTo(followeeId);
            assertThat(result.followerCount()).isEqualTo(5);
            assertThat(result.followingCount()).isEqualTo(10);

            assertThat(result.followedByMe()).isFalse();
            assertThat(result.followedByMeId()).isNull();
            assertThat(result.followingMe()).isFalse();
        }

        @Test
        @DisplayName("팔로우 한 상태")
        void getFollowSummary_following() {
            // given
            UUID myId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();

            OtbooUserDetails userDetails = mock(OtbooUserDetails.class);
            UserResponse userResponse = UserResponse.builder().id(myId).build();

            Follow follow = mock(Follow.class);
            UUID followId = UUID.randomUUID();

            given(userDetails.getUserResponse()).willReturn(userResponse);
            given(userRepository.findById(followeeId)).willReturn(Optional.of(mock(User.class)));

            given(followRepository.countByFollowerId(followeeId)).willReturn(10);
            given(followRepository.countByFolloweeId(followeeId)).willReturn(5);

            given(follow.getId()).willReturn(followId);

            // 팔로우 관계 있음
            given(followRepository.findByFollowerIdAndFolloweeId(myId, followeeId))
                .willReturn(Optional.of(follow));

            // when
            FollowSummaryResponse result =
                followService.getFollowSummary(followeeId, userDetails);

            // then
            assertThat(result.followedByMe()).isTrue();
            assertThat(result.followedByMeId()).isEqualTo(followId);
            assertThat(result.followingMe()).isTrue();
        }
    }

    @Nested
    @DisplayName("팔로우 목록 조회")
    class GetFollows {

        @Test
        @DisplayName("팔로잉 조회 성공")
        void getFollowings_success() {
            // given
            CursorRequest cursorRequest = new CursorRequest(null, null, 2);

            LocalDateTime now = LocalDateTime.now();

            List<FollowDto> list = List.of(
                createDto(now),
                createDto(now.minusSeconds(10))
            );

            given(followRepository.findAllFollowings(
                any(), anyString(), any(), any(), any()
            )).willReturn(list);

            given(followMapper.toDto((FollowDto) any())).willReturn(
                FollowResponse.builder().id(UUID.randomUUID()).build()
            );

            // when
            CursorResponse<FollowResponse> result =
                followService.getFollowings(UUID.randomUUID(), "", cursorRequest);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("팔로우 취소")
    class CancelFollow {

        @Test
        @DisplayName("팔로우 취소 성공")
        void cancelFollow_success() {
            // when
            followService.cancelFollow(followerId);

            // then
            verify(followRepository).deleteById(followerId);
        }
    }

    private FollowDto createDto(LocalDateTime createdAt) {
        return FollowDto.builder()
            .id(UUID.randomUUID())
            .createdAt(createdAt)
            .followeeId(UUID.randomUUID())
            .followeeName("followee")
            .followeeProfileImageId(UUID.randomUUID())
            .followerId(UUID.randomUUID())
            .followerName("follower")
            .followerProfileImageId(UUID.randomUUID())
            .build();
    }

}