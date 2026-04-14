package com.codeit.otboo.domain.like.service;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.exception.FeedNotFoundException;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.like.entity.Like;
import com.codeit.otboo.domain.like.exception.LikeAlreadyExistsException;
import com.codeit.otboo.domain.like.exception.LikeNotFoundException;
import com.codeit.otboo.domain.like.repository.LikeRepository;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.exception.ErrorCode;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    LikeRepository likeRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    FeedRepository feedRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    LikeServiceImpl likeService;

    private Feed feed;
    private User user;

    @BeforeEach
    void setUp() {
        User author = User.builder().build();
        ReflectionTestUtils.setField(author, "id", UUID.randomUUID());

        feed = Feed.builder().author(author).build();
        ReflectionTestUtils.setField(feed, "id", UUID.randomUUID());

        user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        new Profile(user, "user");

    }

    @Nested
    @DisplayName("피드 좋아요")
    class FeedLike {

        @Test
        @DisplayName("좋아요를 누를 수 있다.")
        void likeFeed_Success() {
            // given
            UUID feedId = feed.getId();
            UUID userId = user.getId();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(likeRepository.existsByFeedIdAndUserId(feedId, userId)).willReturn(false);

            // when
            likeService.feedLike(feedId, userId);

            // then
            verify(likeRepository, times(1)).save(any(Like.class));
            assertThat(feed.getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하지 않는 피드Id라면 예외를 반환한다.")
        void likeFeed_Fail_NotFoundFeed() {
            // given
            UUID feedId = feed.getId();
            UUID userId = user.getId();

            given(feedRepository.findById(feedId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> likeService.feedLike(feedId, userId))
                    .isInstanceOf(FeedNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FEED_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 유저Id라면 예외를 반환한다.")
        void likeFeed_Fail_NotFoundUser() {
            // given
            UUID feedId = feed.getId();
            UUID userId = user.getId();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(Feed.builder().build()));
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> likeService.feedLike(feedId, userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("좋아요가 이미 존재한다면 예외를 반환한다.")
        void likeFeed_Fail_AlreadyLiked() {
            // given
            UUID feedId = feed.getId();
            UUID userId = user.getId();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(Feed.builder().build()));
            given(userRepository.findById(userId)).willReturn(Optional.of(User.builder().build()));
            given(likeRepository.existsByFeedIdAndUserId(feedId, userId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> likeService.feedLike(feedId, userId))
                    .isInstanceOf(LikeAlreadyExistsException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.LIKE_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("")
    class FeedUnlike {

        @Test
        @DisplayName("좋아요를 취소할 수 있다.")
        void unlikeFeed_Success() {
            // given
            UUID feedId = feed.getId();
            UUID userId = user.getId();

            Like like = Like.builder().feed(feed).user(user).build();
            feed.increaseLike();

            given(likeRepository.findByFeedIdAndUserId(feedId, userId)).willReturn(Optional.of(like));
            // when
            likeService.feedUnlike(feedId, userId);

            // then
            verify(likeRepository, times(1)).delete(like);
            assertThat(feed.getLikeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("좋아요가 존재하지 않으면 예외를 반환한다.")
        void unlikeFeed_Fail_NotFoundLike() {
            // given
            UUID feedId = feed.getId();
            UUID userId = user.getId();

            given(likeRepository.findByFeedIdAndUserId(feedId, userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> likeService.feedUnlike(feedId, userId))
                    .isInstanceOf(LikeNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.LIKE_NOT_FOUND);
        }
    }

}