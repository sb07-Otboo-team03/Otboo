package com.codeit.otboo.domain.feed.repository;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.entity.Feed;
import org.springframework.data.domain.Slice;

public interface FeedRepositoryCustom {
    Slice<Feed> findAllByKeywordLike(FeedSearchCondition condition);

    long countTotalElements(FeedSearchCondition condition);
}
