//package com.codeit.otboo.domain.follow.unit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.codeit.otboo.domain.BaseEntity;
//import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
//import com.codeit.otboo.domain.directmessage.util.TestFixture;
//import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
//import com.codeit.otboo.domain.follow.dto.FollowDto;
//import com.codeit.otboo.domain.follow.dto.FollowResponse;
//import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
//import com.codeit.otboo.domain.follow.entity.Follow;
//import com.codeit.otboo.domain.follow.exception.follow.DuplicateFollowException;
//import com.codeit.otboo.domain.follow.exception.follow.FollowNotFoundException;
//import com.codeit.otboo.domain.follow.mapper.FollowMapper;
//import com.codeit.otboo.domain.follow.repository.FollowRepository;
//import com.codeit.otboo.domain.follow.service.FollowServiceImpl;
//import com.codeit.otboo.domain.notification.dto.NotificationDto;
//import com.codeit.otboo.domain.notification.dto.NotificationLevel;
//import com.codeit.otboo.domain.profile.entity.Profile;
//import com.codeit.otboo.domain.sse.event.FollowSseEvent;
//import com.codeit.otboo.domain.user.dto.response.UserResponse;
//import com.codeit.otboo.domain.user.entity.User;
//import com.codeit.otboo.domain.user.exception.UserNotFoundException;
//import com.codeit.otboo.domain.user.repository.UserRepository;
//import com.codeit.otboo.global.security.OtbooUserDetails;
//import com.codeit.otboo.global.slice.dto.CursorResponse;
//import java.lang.reflect.Field;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.data.domain.Pageable;
//
//@DisplayName("🎯Unit Test >>> FollowServiceImpl")
//@ExtendWith(MockitoExtension.class)
//class FollowServiceImplTest {
//
//    @InjectMocks
//    private FollowServiceImpl followService;
//
//    @Mock
//    private FollowRepository followRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private FollowMapper followMapper;
//
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//
//    // =========================
//    // ✅ CREATE
//    // =========================
//
////    @Test
////    void create_newFollow() {
////        UUID followerId = UUID.randomUUID();
////        UUID followeeId = UUID.randomUUID();
////
////        FollowCreateRequest request =
////            new FollowCreateRequest(followerId, followeeId);
////
////        User follower = User.builder()
////            .email("follower@test.com")
////            .password("1234")
////            .build();
////
////        User followee = User.builder()
////            .email("followee@test.com")
////            .password("1234")
////            .build();
////
////        Profile profile = Profile.builder()
////            .user(follower)
////            .name("테스트유저")
////            .build();
////
////        Follow savedFollow = mock(Follow.class);
////        FollowResponse response = mock(FollowResponse.class);
////
////        when(followRepository.findByFollowerIdAndFolloweeId(
////            followerId, followeeId
////        )).thenReturn(Optional.empty());
////
////        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
////        when(userRepository.findById(followeeId)).thenReturn(Optional.of(followee));
////
////        when(followRepository.save(any(Follow.class))).thenReturn(savedFollow);
////        when(savedFollow.getId()).thenReturn(UUID.randomUUID());
////        when(followMapper.toDto(any(Follow.class))).thenReturn(response);
////
////        FollowResponse result = followService.create(request);
////
////        assertThat(result).isEqualTo(response);
////    }
//
//    @Test
//    @DisplayName("✅ create - 기존 follow 존재 → 재활성화")
//    void create_existingFollow() {
//        // given
//        UUID followerId = UUID.randomUUID();
//        UUID followeeId = UUID.randomUUID();
//
//        FollowCreateRequest request =
//            new FollowCreateRequest(followerId, followeeId);
//
//        Follow existingFollow = mock(Follow.class);
//        FollowResponse response = mock(FollowResponse.class);
//
//        UUID followId = UUID.randomUUID();
//
//        when(followRepository.findByFollowerIdAndFolloweeId(
//            followerId, followeeId
//        )).thenReturn(Optional.of(existingFollow));
//
//        when(existingFollow.getId()).thenReturn(followId);
//
//        when(followMapper.toDto(existingFollow)).thenReturn(response);
//
//        // when
//        FollowResponse result = followService.create(request);
//
//        // then
//        assertThat(result).isEqualTo(response);
//
//        verify(followRepository).updateIsActive(followId, true);
//    }
//
//    // =========================
//    // ✅ getFollowSummary
//    // =========================
//
//    @Test
//    @DisplayName("✅ getFollowSummary - 나 자신")
//    void getFollowSummary_me() {
//        UUID myId = UUID.randomUUID();
//
//        OtbooUserDetails userDetails = mock(OtbooUserDetails.class);
//        UserResponse userResponse = mock(UserResponse.class);
//
//        when(userDetails.getUserResponse()).thenReturn(userResponse);
//        when(userResponse.id()).thenReturn(myId);
//
//        when(userRepository.findById(myId))
//            .thenReturn(Optional.of(mock(User.class)));
//
//        when(followRepository.countByFollowerIdAndIsActiveTrue(myId)).thenReturn(10);
//        when(followRepository.countByFolloweeIdAndIsActiveTrue(myId)).thenReturn(5);
//
//        FollowSummaryResponse result =
//            followService.getFollowSummary(myId, userDetails);
//
//        assertThat(result.followedByMe()).isFalse();
//        assertThat(result.followingMe()).isFalse();
//        assertThat(result.followedByMeId()).isNull();
//    }
//
//    @Test
//    @DisplayName("✅ getFollowSummary - 타인 & 팔로우 안함")
//    void getFollowSummary_notFollowing() {
//        UUID myId = UUID.randomUUID();
//        UUID targetId = UUID.randomUUID();
//
//        OtbooUserDetails userDetails = mock(OtbooUserDetails.class);
//        UserResponse userResponse = mock(UserResponse.class);
//        Follow follow = mock(Follow.class);
//
//        when(userDetails.getUserResponse()).thenReturn(userResponse);
//        when(userResponse.id()).thenReturn(myId);
//
//        when(userRepository.findById(targetId))
//            .thenReturn(Optional.of(mock(User.class)));
//
//        when(followRepository.countByFollowerIdAndIsActiveTrue(targetId)).thenReturn(10);
//        when(followRepository.countByFolloweeIdAndIsActiveTrue(targetId)).thenReturn(5);
//
//        when(followRepository.findByFollowerIdAndFolloweeId(myId, targetId))
//            .thenReturn(Optional.of(follow));
//
//        when(follow.isActive()).thenReturn(false);
//        when(follow.getId()).thenReturn(UUID.randomUUID());
//
//        FollowSummaryResponse result =
//            followService.getFollowSummary(targetId, userDetails);
//
//        assertThat(result.followedByMe()).isFalse();
//        assertThat(result.followingMe()).isFalse();
//    }
//
//    @Test
//    @DisplayName("✅ getFollowSummary - 타인 & 팔로우 중")
//    void getFollowSummary_following() {
//        UUID myId = UUID.randomUUID();
//        UUID targetId = UUID.randomUUID();
//
//        OtbooUserDetails userDetails = mock(OtbooUserDetails.class);
//        UserResponse userResponse = mock(UserResponse.class);
//        Follow follow = mock(Follow.class);
//
//        when(userDetails.getUserResponse()).thenReturn(userResponse);
//        when(userResponse.id()).thenReturn(myId);
//
//        when(userRepository.findById(targetId))
//            .thenReturn(Optional.of(mock(User.class)));
//
//        when(followRepository.countByFollowerIdAndIsActiveTrue(targetId)).thenReturn(10);
//        when(followRepository.countByFolloweeIdAndIsActiveTrue(targetId)).thenReturn(5);
//
//        when(followRepository.findByFollowerIdAndFolloweeId(myId, targetId))
//            .thenReturn(Optional.of(follow));
//
//        when(follow.isActive()).thenReturn(true);
//        when(follow.getId()).thenReturn(UUID.randomUUID());
//
//        FollowSummaryResponse result =
//            followService.getFollowSummary(targetId, userDetails);
//
//        assertThat(result.followedByMe()).isTrue();
//        assertThat(result.followingMe()).isTrue();
//    }
//
//    // =========================
//    // ✅ cancelFollow
//    // =========================
//
//    @Test
//    @DisplayName("✅ cancelFollow")
//    void cancelFollow() {
//        UUID followId = UUID.randomUUID();
//
//        followService.cancelFollow(followId);
//
//        verify(followRepository).updateIsActive(followId, false);
//    }
//
//    // =========================
//    // ✅ getFollowings / Followers
//    // =========================
//
////    @Test
////    @DisplayName("✅ getFollowings")
////    void getFollowings() {
////        UUID followId = UUID.randomUUID();
////
////        CursorRequest cursorRequest =
////            new CursorRequest(null, null, 2);
////
////        FollowDto dto1 = mock(FollowDto.class);
////        FollowDto dto2 = mock(FollowDto.class);
////        FollowDto dto3 = mock(FollowDto.class);
////
////        when(dto1.createdAt()).thenReturn(LocalDateTime.now());
////        when(dto2.createdAt()).thenReturn(LocalDateTime.now().minusSeconds(1));
////        when(dto3.createdAt()).thenReturn(LocalDateTime.now().minusSeconds(2));
////
////        when(dto1.id()).thenReturn(UUID.randomUUID());
////        when(dto2.id()).thenReturn(UUID.randomUUID());
////        when(dto3.id()).thenReturn(UUID.randomUUID());
////
////        when(followRepository.findAllFollowings(
////            any(), any(), any(), any(), any()
////        )).thenReturn(List.of(dto1, dto2, dto3));
////
////        when(followMapper.toDto(any())).thenReturn(mock(FollowResponse.class));
////
////        CursorResponse<FollowResponse> result =
////            followService.getFollowings(followId, null, cursorRequest);
////
////        assertThat(result.data()).hasSize(2);
////        assertThat(result.hasNext()).isTrue();
////    }
//
//    @Test
//    @DisplayName("✅ getFollowers")
//    void getFollowers() {
//        UUID followId = UUID.randomUUID();
//
//        CursorRequest cursorRequest =
//            new CursorRequest(null, null, 2);
//
//        when(followRepository.findAllFollowers(
//            any(), any(), any(), any(), any()
//        )).thenReturn(List.of());
//
//        CursorResponse<FollowResponse> result =
//            followService.getFollowers(followId, null, cursorRequest);
//
//        assertThat(result.data()).isEmpty();
//    }
//}