package com.codeit.otboo.domain.like.repository;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.fixture.FeedFixture;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.like.entity.Like;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.config.TestJpaAuditing;
import com.codeit.otboo.global.config.TestQueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestQueryDslConfig.class, TestJpaAuditing.class})
@ActiveProfiles("test")
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private FeedRepository feedRepository;

    private User user;
    private List<Feed> feedList;

    @BeforeEach
    void setUp() {
        user = new User("otboo@a.a", "otboo123");
        entityManager.persist(user);

        feedList = FeedFixture.createFeed(3, user);
        feedRepository.saveAll(feedList);
    }

    @Test
    @DisplayName("피드 ID와 유저(나) ID로 (내가)좋아요를 누른 피드를 찾을 수 있다.")
    void findLikeByFeedIdAndUserId() {
        // given
        Feed feed1 = feedList.get(0);
        Feed feed2 = feedList.get(1);
        Feed feed3 = feedList.get(2);
        List<UUID> feedIdList = List.of(feed1.getId(), feed2.getId(), feed3.getId());

        Like like1 = new Like(user, feed1);
        Like like2 = new Like(user, feed3);
        likeRepository.saveAll(List.of(like1, like2));

        // when
        Set<UUID> likedFeedIds = likeRepository.findFeedIdsByUserIdAndFeedIdIn(user.getId(), feedIdList);

        // then
        assertThat(likedFeedIds.size()).isEqualTo(2);
        assertThat(likedFeedIds).containsExactlyInAnyOrder(feed1.getId(), feed3.getId());
    }

    @Test
    @DisplayName("피드 ID로 모든 좋아요를 삭제한다.")
    void deleteAllLikeByFeedId() {
        // given
        Feed feed = feedList.get(0);
        Feed safeFeed = feedList.get(1);

        User user2 = new User("codeit@a.a", "codeit123");
        entityManager.persist(user2);

        Like like1 = new Like(user, feed);
        Like like2 = new Like(user2, feed);
        Like safeLike = new Like(user2, safeFeed);
        likeRepository.saveAll(List.of(like1, like2, safeLike));

        // when
        likeRepository.deleteAllByFeedId(feed.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(likeRepository.count()).isEqualTo(1);
    }
}