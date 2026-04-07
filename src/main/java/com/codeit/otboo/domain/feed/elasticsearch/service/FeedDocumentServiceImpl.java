package com.codeit.otboo.domain.feed.elasticsearch.service;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.elasticsearch.document.FeedDocument;
import com.codeit.otboo.domain.feed.elasticsearch.event.FeedSyncEvent;
import com.codeit.otboo.domain.feed.elasticsearch.repository.FeedDocumentRepository;
import com.codeit.otboo.domain.feed.entity.Feed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedDocumentServiceImpl implements FeedDocumentService {

    private final FeedDocumentRepository feedDocumentRepository;

    @Override
    public void toDocument(FeedSyncEvent event) {
        FeedDocument feedDocument = FeedDocument.builder()
                .id(event.feedId().toString())
                .content(event.content())
                .skyStatus(event.skyStatus())
                .precipitationType(event.precipitationType())
                .likeCount(event.likeCount())
                .createdAt(getEpochMilli(event.createdAt()))
                .build();
        feedDocumentRepository.save(feedDocument);
    }

    @Override
    public SearchHits<FeedDocument> getAllByElasticsearch(FeedSearchCondition condition) {
        return feedDocumentRepository.searchFeed(condition);
    }

    @Override
    public void updateLikeCount(UUID feedId, long newLikeCount) {
        feedDocumentRepository.findById(feedId.toString())
                .ifPresentOrElse(
                doc -> {
                    doc.updateLikeCount(newLikeCount);
                    feedDocumentRepository.save(doc);
                }, () ->
                        NotFoundDocErrorLog(feedId)
        );
    }

    @Override
    public void updateContent(UUID feedId, String newContent) {
        feedDocumentRepository.findById(feedId.toString())
                .ifPresentOrElse(doc -> {
                    doc.updateContent(newContent);
                    feedDocumentRepository.save(doc);
                }, () ->
                        NotFoundDocErrorLog(feedId)
        );
    }

    @Override
    public void deleteFeed(UUID feedId) {
        feedDocumentRepository.findById(feedId.toString())
                .ifPresentOrElse(doc ->
                            feedDocumentRepository.deleteById(feedId.toString())
                        , () ->
                                NotFoundDocErrorLog(feedId)
                );
    }

    @Override
    public void syncAllFeeds(List<Feed> recentFeeds) {
        feedDocumentRepository.saveAll(recentFeeds.stream().map(
                feed -> new FeedDocument(feed.getId().toString(), feed.getContent(),
                        feed.getWeather().getSkyStatus().name(), feed.getWeather().getPrecipitationType().name(),
                        feed.getLikeCount(), getEpochMilli(feed.getCreatedAt()))
        ).toList());
    }

    private void NotFoundDocErrorLog(UUID feedId) {
        log.warn("Elasticsearch가 Feed 문서를 찾지 못했습니다. feedId= {}", feedId);
    }
    private long getEpochMilli(LocalDateTime localDateTime) {
        return localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}
