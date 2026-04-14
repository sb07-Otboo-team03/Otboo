package com.codeit.otboo.domain.feed.elasticsearch.repository;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.elasticsearch.document.FeedDocument;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface FeedDocumentRepositoryCustom {
    SearchHits<FeedDocument> searchFeed(FeedSearchCondition condition);

}
