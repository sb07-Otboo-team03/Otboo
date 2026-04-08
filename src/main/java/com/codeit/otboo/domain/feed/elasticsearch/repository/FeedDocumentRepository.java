package com.codeit.otboo.domain.feed.elasticsearch.repository;

import com.codeit.otboo.domain.feed.elasticsearch.document.FeedDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedDocumentRepository extends ElasticsearchRepository<FeedDocument, String>,
        FeedDocumentRepositoryCustom {
}
