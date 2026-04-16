package com.codeit.otboo.domain.feed.repository;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.fixture.FeedFixture;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.config.TestJpaAuditing;
import com.codeit.otboo.global.config.TestQueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestQueryDslConfig.class, TestJpaAuditing.class})
@ActiveProfiles("test")
class FeedRepositoryTest {

    @Autowired
    private FeedRepository feedRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("피드의 댓글 수를 알 수 있다.")
    void findCommentCount() {
        // given
        User author = new User("author@a.a", "otboo123");
        entityManager.persist(author);
        Feed feed = FeedFixture.createFeed(1, author).get(0);
        ReflectionTestUtils.setField(feed, "commentCount", 5);
        entityManager.persist(feed);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Integer> result = feedRepository.findCommentCountByFeedId(feed.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(5);
    }

    @Test
    @DisplayName("피드가 존재하지 않으면 빈 Optional을 반환한다.")
    void findCommentCount_NotFound() {
        // given
        UUID feedId = UUID.randomUUID();

        // when
        Optional<Integer> result = feedRepository.findCommentCountByFeedId(feedId);

        // then
        assertThat(result).isEmpty();
    }
}