package com.codeit.otboo.domain.feed.repository;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.entity.Feed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FeedRepositoryCustomImpl implements FeedRepositoryCustom{

//    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Feed> findAllByKeywordLike(FeedSearchCondition condition) {
        return null;
    }

    @Override
    public long countTotalElements(FeedSearchCondition condition) {
        return 0;
    }
}
