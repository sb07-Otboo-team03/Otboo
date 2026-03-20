package com.codeit.otboo.domain.like.service;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.like.entity.Like;
import com.codeit.otboo.domain.like.repository.LikeRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    @InjectMocks
    LikeServiceImpl likeService;

    private UUID feedId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        UUID feedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("피드 좋아요")
    class FeedLike {

        @Test
        @DisplayName("좋아요를 누를 수 있다.")
        void likeFeed() {
            // given
            Feed feed = Feed.builder().build();
            User user = User.builder().build();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(likeRepository.existsByFeedIdAndUserId(feedId, userId)).willReturn(false);

            // when
            likeService.feedLike(feedId, userId);

            // then
            verify(likeRepository, times(1)).save(any(Like.class));
            assertThat(feed.getLikeCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("")
    class FeedUnlike {

        @Test
        @DisplayName("좋아요를 취소할 수 있다.")
        void unlikeFeed() {
            // given

            Feed feed = Feed.builder().build();
            User user = User.builder().build();

            Like like = Like.builder().feed(feed).user(user).build();
            feed.increaseLike();

            given(likeRepository.findByFeedIdAndUserId(feedId, userId)).willReturn(Optional.of(like));
            // when
            likeService.feedUnlike(feedId, userId);

            // then
            verify(likeRepository, times(1)).delete(like);
            assertThat(feed.getLikeCount()).isEqualTo(0);
        }
    }

}