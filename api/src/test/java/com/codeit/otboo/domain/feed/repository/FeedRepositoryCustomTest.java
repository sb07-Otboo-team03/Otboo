package com.codeit.otboo.domain.feed.repository;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.dto.type.SortBy;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.fixture.FeedFixture;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import com.codeit.otboo.global.config.TestJpaAuditing;
import com.codeit.otboo.global.config.TestQueryDslConfig;
import com.codeit.otboo.global.slice.dto.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestQueryDslConfig.class, TestJpaAuditing.class})
@ActiveProfiles("test")
class FeedRepositoryCustomTest {

    @Autowired
    private FeedRepository feedRepository;
    @Autowired
    private TestEntityManager entityManager;

    private void saveFeed(int n) {
        User user = new User("otboo@a.a", "otboo123");
        entityManager.persist(user);

        List<Feed> feedList = FeedFixture.createFeed(n, user);
        feedList.forEach(entityManager::persist);
        entityManager.flush();

        LocalDateTime baseTime = LocalDateTime.now();
        var em = entityManager.getEntityManager();

        for (int i = 0; i < n; i++) {
            Feed feed = feedList.get(i);
            em.createQuery("update Feed f set f.createdAt = :createdAt where f.id = :id")
                    .setParameter("createdAt", baseTime.minusDays(i))
                    .setParameter("id", feed.getId())
                    .executeUpdate();
        }

        entityManager.clear();
    }

    private String createCursor(Feed feed, String sortBy) {
        if ("createdAt".equals(sortBy)) return String.valueOf(feed.getCreatedAt());
        else return String.valueOf(feed.getLikeCount());
    }

    @Nested
    @DisplayName("주정렬과 보조정렬의 오름, 내림차순")
    class SortTest {

        @ParameterizedTest
        @CsvSource({
                "createdAt, DESCENDING",
                "createdAt, ASCENDING",

                "likeCount, DESCENDING",
                "likeCount, ASCENDING"

        })
        @DisplayName("""
                주정렬: 생성일, 좋아요 수
                보조정렬: id
                순서: 내림차순, 오름차순
                검색어: 전체
                날씨: 전체
                강수: 전체
                """)
        void searchFeedBySort(String sortBy, SortDirection sortDirection) {
            // given
            saveFeed(7);

            // firstPage
            FeedSearchCondition condition1 = FeedSearchCondition.builder()
                    .limit(5)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();

            Slice<Feed> firstPage = feedRepository.findAllByKeywordLike(condition1);
            List<Feed> firstPageList = firstPage.getContent();
            Feed lastFeed = firstPageList.get(firstPageList.size() - 1);
            String lastCursor = createCursor(lastFeed, sortBy);
            UUID lastId = lastFeed.getId();

            // secondPage
            FeedSearchCondition condition2
                    = FeedSearchCondition.builder()
                    .cursor(lastCursor)
                    .idAfter(lastId)
                    .limit(5)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            Slice<Feed> secondPage = feedRepository.findAllByKeywordLike(condition2);
            List<Feed> secondPageList = secondPage.getContent();

            // then
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(firstPageList.size()).isEqualTo(5);
            assertThat(secondPageList.size()).isEqualTo(2);

            if ("createdAt".equals(sortBy)) {
                if (SortDirection.DESCENDING.equals(sortDirection))
                    assertThat(secondPageList.get(0).getCreatedAt()).isBefore(lastFeed.getCreatedAt());
                else
                    assertThat(secondPageList.get(0).getCreatedAt()).isAfter(lastFeed.getCreatedAt());
            } else {
                if (SortDirection.DESCENDING.equals(sortDirection))
                    assertThat(secondPageList.get(0).getLikeCount()).isLessThan(lastFeed.getLikeCount());
                else
                    assertThat(secondPageList.get(0).getLikeCount()).isGreaterThan(lastFeed.getLikeCount());
            }
        }

        @Test
        @DisplayName("""
                cursor 혹은 idAfter가 null 이라면 전체를 조회한다.
                """)
        void searchFeedBySort_NullCursorOrNullIdAfter() {
            // given
            saveFeed(5);
            FeedSearchCondition condition1 = FeedSearchCondition.builder()
                    .limit(5)
                    .cursor(LocalDateTime.now().toString())
                    .sortBy(SortBy.CREATED_AT.name())
                    .sortDirection(SortDirection.DESCENDING)
                    .build();

            Slice<Feed> page = feedRepository.findAllByKeywordLike(condition1);
            List<Feed> list = page.getContent();

            FeedSearchCondition condition2 = FeedSearchCondition.builder()
                    .limit(5)
                    .idAfter(UUID.randomUUID())
                    .sortBy(SortBy.CREATED_AT.name())
                    .sortDirection(SortDirection.DESCENDING)
                    .build();

            Slice<Feed> page2 = feedRepository.findAllByKeywordLike(condition2);
            List<Feed> list2 = page2.getContent();

            // then
            assertThat(list.size()).isEqualTo(5);
            assertThat(list2.size()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("검색어로 조회")
    class SearchByKeywordTest {

        @ParameterizedTest
        @CsvSource(value = {
                "가", "null", "''"
        }, nullValues = "null")
        @DisplayName("""
                주정렬: 생성일
                보조정렬: id
                순서: 내림차순
                검색어: 가, (null 혹은 공백)
                날씨: 전체
                강수: 전체
                """)
        void searchFeedByKeyword(String keyword) {
            // given
            saveFeed(5);
            FeedSearchCondition condition
                    = FeedSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .keywordLike(keyword)
                    .build();
            Slice<Feed> page = feedRepository.findAllByKeywordLike(condition);
            List<Feed> list = page.getContent();

            // then
            if (keyword == null || keyword.isBlank())
                assertThat(list.size()).isEqualTo(5);
            else {
                assertThat(list.size()).isEqualTo(3);
                assertThat(list.get(0).getContent()).contains("가");
            }
        }
    }

    @Nested
    @DisplayName("날씨 정보로 필터링")
    class SearchByWeatherTest {

        @Test
        @DisplayName("""
                주정렬: 생성일
                보조정렬: id
                순서: 내림차순
                검색어: 전체
                날씨: 맑음
                강수: 없음
                """)
        void searchFeedByWeather() {
            // given
            saveFeed(5);
            FeedSearchCondition condition = FeedSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .skyStatusEqual(SkyStatus.CLEAR)
                    .precipitationTypeEqual(PrecipitationType.NONE)
                    .build();
            Slice<Feed> page = feedRepository.findAllByKeywordLike(condition);
            List<Feed> list = page.getContent();

            // then
            assertThat(list.size()).isEqualTo(3);
            assertThat(list.get(0).getWeather().getSkyStatus()).isEqualTo(SkyStatus.CLEAR);
            assertThat(list.get(2).getWeather().getPrecipitationType()).isEqualTo(PrecipitationType.NONE);
        }
    }

    @Nested
    @DisplayName("자신(author)의 피드 조회")
    class SearchByAuthorTest {

        @Test
        @DisplayName("""
                프로필에서 자신의 피드를 조회할 수 있다.
                AuthorIdEqual == authorId
                """)
        void searchFeedByAuthor() {
            // given
            saveFeed(5);
            User author = new User("author@a.a", "otboo123");
            entityManager.persist(author);
            List<Feed> feedList = FeedFixture.createFeed(3, author);
            feedList.forEach(entityManager::persist);

            entityManager.flush();
            entityManager.clear();

            FeedSearchCondition condition = FeedSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .authorIdEqual(author.getId())
                    .build();

            Slice<Feed> page = feedRepository.findAllByKeywordLike(condition);
            List<Feed> list = page.getContent();

            // then
            assertThat(list.size()).isEqualTo(3);
            assertThat(list).extracting(feed -> feed.getAuthor().getId())
                    .containsOnly(author.getId());
        }
    }

    @Nested
    @DisplayName("총 갯수")
    class CountTest {

        @Test
        @DisplayName("피드의 총 갯수를 알 수 있다.")
        void countTotalFeeds() {
            // given
            saveFeed(10);
            FeedSearchCondition condition = FeedSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .build();

            long total = feedRepository.countTotalElements(condition);

            // then
            assertThat(total).isEqualTo(10);
        }
    }
}