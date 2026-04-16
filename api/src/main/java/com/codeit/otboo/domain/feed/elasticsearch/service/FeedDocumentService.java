package com.codeit.otboo.domain.feed.elasticsearch.service;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.elasticsearch.document.FeedDocument;
import com.codeit.otboo.domain.feed.elasticsearch.event.FeedSyncEvent;
import com.codeit.otboo.domain.feed.entity.Feed;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;
import java.util.UUID;

public interface FeedDocumentService {
    void toDocument(FeedSyncEvent event);
    SearchHits<FeedDocument> getAllByElasticsearch(FeedSearchCondition condition);
    void updateLikeCount(UUID feedId, long newLikeCount);
    void updateContent(UUID feedId, String newContent);
    void deleteFeed(UUID feedId);

    void syncAllFeeds(List<Feed> recentFeeds);
}
