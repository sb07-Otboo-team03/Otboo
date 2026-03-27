package com.codeit.otboo.domain.comment.repository;

import com.codeit.otboo.domain.comment.entity.Comment;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.fixture.FeedFixture;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.config.TestJpaAuditing;
import com.codeit.otboo.global.config.TestQueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestQueryDslConfig.class, TestJpaAuditing.class})
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private FeedRepository feedRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private List<Feed> feedList;

    @BeforeEach
    void setUp() {
        user = new User("otboo@a.a", "otboo123");
        entityManager.persist(user);
        feedList = FeedFixture.createFeed(2, user);
        feedRepository.saveAll(feedList);
    }

    void createComment(Feed feed, User user, int n) {
        List<Comment> commentList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Comment comment = new Comment("comment " + i, feed, user);
            ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now().minusDays(i));
            commentList.add(comment);
        }
        commentRepository.saveAll(commentList);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("""
            피드Id로 댓글을 조회할 수 있다.
            cursor: 생성일
            idAfter: id
            limit: 5
            정렬: 내림차순
            """)
    void searchCommentCursorTest() {
        // given
        Feed feed = feedList.get(0);
        createComment(feed, user, 7);

        // page 1
        Slice<Comment> page1 = commentRepository.findAllByCursor(feed.getId(), null, null, 5);
        List<Comment> comments1 = page1.getContent();
        Comment lastComment = comments1.get(comments1.size() - 1);

        String lastCursor = lastComment.getCreatedAt().toString();
        UUID lastId = lastComment.getId();

        // page 2
        Slice<Comment> page2 = commentRepository.findAllByCursor(feed.getId(), lastCursor, lastId, 5);
        List<Comment> comments2 = page2.getContent();

        // then
        assertThat(comments1.size()).isEqualTo(5);
        assertThat(page1.hasNext()).isTrue();
        assertThat(comments2.size()).isEqualTo(2);
        assertThat(page2.hasNext()).isFalse();
        assertThat(comments2.get(0).getCreatedAt()).isBefore(lastComment.getCreatedAt());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "2026-03-24 10:00:00.000000, null",
            "null, 6de3b7bc-b14c-4608-a343-2b16a27a7dd0"
    }, nullValues = "null")
    @DisplayName("""
            댓글 조회 시, cursor 혹은 idAfter가 null이라면
            전체를 조회한다.
            """)
    void searchCommentByCursor_NullCursorOrNullIdAfter(String cursor, UUID idAfter) {
        // given
        Feed feed = feedList.get(0);
        createComment(feed, user, 7);

        // when
        Slice<Comment> page1 = commentRepository.findAllByCursor(feed.getId(), cursor, idAfter, 5);

        // then
        assertThat(page1.getContent().size()).isEqualTo(5);
        assertThat(page1.hasNext()).isTrue();
    }

    @Test
    @DisplayName("댓글의 전체 수를 알 수 있다.")
    void countTotalComments() {
        // given
        Feed feed = feedList.get(0);
        Feed feed2 = feedList.get(1);

        createComment(feed, user, 7);
        createComment(feed2, user, 3);

        // when
        long count1 = commentRepository.countTotalElements(feed.getId());
        long count2 = commentRepository.countTotalElements(feed2.getId());


        // then
        assertThat(count1).isEqualTo(7);
        assertThat(count2).isEqualTo(3);
    }

    @Test
    @DisplayName("피드 ID로 모든 댓글을 삭제할 수 있다.")
    void deleteAllByFeedId() {
        // given
        Feed feed = feedList.get(0);
        createComment(feed, user, 4);

        // when
        commentRepository.deleteAllByFeedId(feed.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(commentRepository.count()).isEqualTo(0);
    }
}