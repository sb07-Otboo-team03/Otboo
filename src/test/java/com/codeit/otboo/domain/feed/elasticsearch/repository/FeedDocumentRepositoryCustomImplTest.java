package com.codeit.otboo.domain.feed.elasticsearch.repository;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.elasticsearch.document.FeedDocument;
import com.codeit.otboo.domain.feed.fixture.FeedDocumentFixture;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import com.codeit.otboo.global.slice.dto.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataElasticsearchTest
@Testcontainers
@ActiveProfiles("test")
class FeedDocumentRepositoryCustomImplTest {

    @Container
    static GenericContainer<?> elasticsearchContainer =
            new GenericContainer<>(
                    new ImageFromDockerfile()
                            // 프로젝트 폴더 최상단에 있는 도커 파일 읽어오기
                            .withDockerfile(Paths.get("elasticsearch/elasticsearch.Dockerfile").toAbsolutePath())
            )
                    .withExposedPorts(9200)
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .waitingFor(Wait.forHttp("/")
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(2))
                    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        String esAddress = elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200);
        registry.add("spring.elasticsearch.uris", () -> esAddress);
    }

    @Autowired
    private FeedDocumentRepository feedDocumentRepository;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void setUp() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(FeedDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        indexOps.putMapping(indexOps.createMapping(FeedDocument.class));
    }

    private void saveDocsToES(List<FeedDocument> docs) {
        feedDocumentRepository.saveAll(docs);

        elasticsearchOperations.indexOps(FeedDocument.class).refresh();
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
            List<FeedDocument> docs = FeedDocumentFixture.createFeedDocument(7);
            saveDocsToES(docs);

            // firstPage
            FeedSearchCondition condition1 = FeedSearchCondition.builder()
                    .limit(5)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();

            SearchHits<FeedDocument> firstPage = feedDocumentRepository.searchFeed(condition1);
            List<SearchHit<FeedDocument>> hits = firstPage.getSearchHits();

            List<Object> lastHitValue = hits.get(4).getSortValues();

            List<FeedDocument> firstPageList = firstPage.stream().map(SearchHit::getContent).toList();
            FeedDocument lastDoc = firstPageList.get(firstPageList.size() - 1);

            String lastCursor = lastHitValue.get(0).toString();
            UUID lastId = UUID.fromString(Objects.requireNonNull(hits.get(4).getId()));
            System.out.println("cursor : " + lastCursor + " id : " + lastId);

            // secondPage
            FeedSearchCondition condition2
                    = FeedSearchCondition.builder()
                    .cursor(lastCursor)
                    .idAfter(lastId)
                    .limit(5)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            SearchHits<FeedDocument> secondPage = feedDocumentRepository.searchFeed(condition2);
            List<FeedDocument> secondPageList = secondPage.stream().map(SearchHit::getContent).toList();

            // then
            assertThat(firstPageList.size()).isEqualTo(6);
            assertThat(secondPageList.size()).isEqualTo(2);

            if ("createdAt".equals(sortBy)) {
                if (SortDirection.DESCENDING.equals(sortDirection))
                    assertThat(secondPageList.get(0).getCreatedAt()).isLessThanOrEqualTo(lastDoc.getCreatedAt());
                else
                    assertThat(secondPageList.get(0).getCreatedAt()).isGreaterThanOrEqualTo(lastDoc.getCreatedAt());
            } else {
                if (SortDirection.DESCENDING.equals(sortDirection))
                    assertThat(secondPageList.get(0).getLikeCount()).isLessThanOrEqualTo(lastDoc.getLikeCount());
                else
                    assertThat(secondPageList.get(0).getLikeCount()).isGreaterThanOrEqualTo(lastDoc.getLikeCount());
            }
        }
    }

    @Nested
    @DisplayName("검색어로 조회")
    class SearchByKeywordTest {

        @ParameterizedTest
        @CsvSource(value = {
                "1", "null", "''"
        }, nullValues = "null")
        @DisplayName("""
                주정렬: 생성일
                보조정렬: id
                순서: 내림차순
                검색어: 1, (null 혹은 공백)
                날씨: 전체
                강수: 전체
                """)
        void searchFeedByKeyword(String keyword) {
            // given
            List<FeedDocument> docs = FeedDocumentFixture.createFeedDocument(5);
            saveDocsToES(docs);

            FeedSearchCondition condition
                    = FeedSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .keywordLike(keyword)
                    .build();

            SearchHits<FeedDocument> page = feedDocumentRepository.searchFeed(condition);
            List<FeedDocument> pageList = page.stream().map(SearchHit::getContent).toList();
            for (FeedDocument feedDocument : pageList) {
                System.out.println(feedDocument.getContent());
            }

            // then
            if (keyword == null || keyword.isBlank())
                assertThat(pageList.size()).isEqualTo(5);
            else {
                assertThat(pageList.size()).isEqualTo(2);
                assertThat(pageList.get(0).getContent()).contains("1");
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
            List<FeedDocument> docs = FeedDocumentFixture.createFeedDocument(3);
            saveDocsToES(docs);
            FeedSearchCondition condition = FeedSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .skyStatusEqual(SkyStatus.CLEAR)
                    .precipitationTypeEqual(PrecipitationType.NONE)
                    .build();
            SearchHits<FeedDocument> page = feedDocumentRepository.searchFeed(condition);
            List<FeedDocument> pageList = page.stream().map(SearchHit::getContent).toList();

            // then
            assertThat(pageList.size()).isEqualTo(2);
            assertThat(pageList.get(0).getSkyStatus()).isEqualTo(SkyStatus.CLEAR.name());
            assertThat(pageList.get(0).getPrecipitationType()).isEqualTo(PrecipitationType.NONE.name());
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
        void searchFeedDocumentByAuthor() {
            //given
            List<FeedDocument> docs = FeedDocumentFixture.createFeedDocument(5);
            UUID authorId = UUID.randomUUID();
            List<FeedDocument> authorDocs = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                docs.add(FeedDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .authorId(authorId.toString())
                        .createdAt(100L - i)
                        .likeCount((long) i)
                        .build());
            }
            saveDocsToES(docs);

            FeedSearchCondition condition = FeedSearchCondition.builder()
                    .limit(5)
                    .sortBy("createdAt")
                    .sortDirection(SortDirection.DESCENDING)
                    .authorIdEqual(authorId)
                    .build();

            SearchHits<FeedDocument> page = feedDocumentRepository.searchFeed(condition);
            List<FeedDocument> pageList = page.stream().map(SearchHit::getContent).toList();

            // then
            assertThat(pageList.size()).isEqualTo(3);
            assertThat(pageList).extracting(FeedDocument::getAuthorId).containsOnly(authorId.toString());
        }
    }
}
