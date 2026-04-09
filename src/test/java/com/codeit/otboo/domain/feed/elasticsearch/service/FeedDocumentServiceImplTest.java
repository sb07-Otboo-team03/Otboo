package com.codeit.otboo.domain.feed.elasticsearch.service;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.elasticsearch.document.FeedDocument;
import com.codeit.otboo.domain.feed.elasticsearch.event.FeedSyncEvent;
import com.codeit.otboo.domain.feed.elasticsearch.repository.FeedDocumentRepository;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedDocumentServiceImplTest {

    @Mock
    private FeedDocumentRepository feedDocumentRepository;

    @InjectMocks
    private FeedDocumentServiceImpl feedDocumentService;

    @Captor
    private ArgumentCaptor<FeedDocument> feedDocumentCaptor;

    @Test
    @DisplayName("피드 생성 이벤트 발생 시 Feed Document를 생성한다.")
    void createFeedDocument() {
        // given
        FeedSyncEvent event = new FeedSyncEvent(UUID.randomUUID(), "content",
                SkyStatus.CLEAR.name(), PrecipitationType.NONE.name(), UUID.randomUUID(), LocalDateTime.now(), 0L);

        // when
        feedDocumentService.toDocument(event);

        // then
        verify(feedDocumentRepository, times(1)).save(feedDocumentCaptor.capture());

        FeedDocument doc = feedDocumentCaptor.getValue();
        assertThat(doc.getId()).isEqualTo(event.feedId().toString());
        assertThat(doc.getContent()).isEqualTo("content");
    }

    @Test
    @DisplayName("FeedDocument를 조회한다.")
    void searchFeedDocument() {
        // given
        FeedSearchCondition condition = FeedSearchCondition.builder().keywordLike("이걸 검사 해야 돼??").build();
        SearchHits<FeedDocument> hits = mock(SearchHits.class);

        given(feedDocumentRepository.searchFeed(condition)).willReturn(hits);

        // when
        SearchHits<FeedDocument> allByElasticsearch = feedDocumentService.getAllByElasticsearch(condition);

        // then
        assertThat(allByElasticsearch).isEqualTo(hits);

    }

    @Nested
    @DisplayName("Content 수정")
    class ContentUpdate {

        @Test
        @DisplayName("Content를 변경한다.")
        void updateFeedDocumentContent() {
            // given
            UUID feedId = UUID.randomUUID();
            String content = "newContent";

            FeedDocument doc = new FeedDocument(feedId.toString(), "content", null,
                    null, UUID.randomUUID().toString(), null, null);

            given(feedDocumentRepository.findById(feedId.toString())).willReturn(Optional.of(doc));

            // when
            feedDocumentService.updateContent(feedId, content);

            // then
            verify(feedDocumentRepository, times(1)).save(doc);
            assertThat(doc.getContent()).isEqualTo(content);
        }

        @Test
        @DisplayName("피드 문서를 찾을 수 없다.")
        void updateContent_FeedDocumentNotFound() {
            // given
            UUID feedId = UUID.randomUUID();

            given(feedDocumentRepository.findById(anyString())).willReturn(Optional.empty());

            // when
            feedDocumentService.updateContent(feedId, "newContent");

            // then
            verify(feedDocumentRepository, never()).save(any(FeedDocument.class));
        }
    }

    @Nested
    @DisplayName("likeCount 수정")
    class LikeCountUpdate {

        @Test
        @DisplayName("likeCount를 변경한다.")
        void updateFeedDocumentContent() {
            // given
            UUID feedId = UUID.randomUUID();
            long newLikeCount = 1L;

            FeedDocument doc = new FeedDocument(feedId.toString(), "content", null,
                    null, UUID.randomUUID().toString(), 0L, null);

            given(feedDocumentRepository.findById(feedId.toString())).willReturn(Optional.of(doc));

            // when
            feedDocumentService.updateLikeCount(feedId, newLikeCount);

            // then
            verify(feedDocumentRepository, times(1)).save(doc);
            assertThat(doc.getLikeCount()).isEqualTo(newLikeCount);
        }

        @Test
        @DisplayName("피드 문서를 찾을 수 없다.")
        void updateContent_FeedDocumentNotFound() {
            // given
            UUID feedId = UUID.randomUUID();

            given(feedDocumentRepository.findById(feedId.toString())).willReturn(Optional.empty());

            // when
            feedDocumentService.updateLikeCount(feedId, 1L);

            // then
            verify(feedDocumentRepository, never()).save(any(FeedDocument.class));
        }
    }

    @Nested
    @DisplayName("FeedDocument 삭제")
    class FeedDocumentDelete {

        @Test
        @DisplayName("likeCount를 변경한다.")
        void deleteFeedDocument() {
            // given
            UUID feedId = UUID.randomUUID();

            FeedDocument doc = new FeedDocument(feedId.toString(), "content", null,
                    null, UUID.randomUUID().toString(), 0L, null);

            given(feedDocumentRepository.findById(feedId.toString())).willReturn(Optional.of(doc));

            // when
            feedDocumentService.deleteFeed(feedId);

            // then
            verify(feedDocumentRepository, times(1)).deleteById(feedId.toString());
        }

        @Test
        @DisplayName("피드 문서를 찾을 수 없다.")
        void updateContent_FeedDocumentNotFound() {
            // given
            UUID feedId = UUID.randomUUID();

            given(feedDocumentRepository.findById(feedId.toString())).willReturn(Optional.empty());

            // when
            feedDocumentService.deleteFeed(feedId);

            // then
            verify(feedDocumentRepository, never()).save(any(FeedDocument.class));
        }
    }
}