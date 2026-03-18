package com.codeit.otboo.domain.feed.repository;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import com.codeit.otboo.global.slice.dto.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.codeit.otboo.domain.feed.entity.QFeed.feed;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FeedRepositoryCustomImpl implements FeedRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Feed> findAllByKeywordLike(FeedSearchCondition condition) {
        int pageSize = condition.limit();

        Pageable pageable = PageRequest.of(0, pageSize);

        Predicate cursorCondition = cursorCondition(
                condition.sortBy(),
                condition.sortDirection(),
                condition.cursor(),
                condition.idAfter()
        );

        List<Feed> contents = queryFactory
                .selectFrom(feed)
                .where(
                        keywordsContains(condition.keywordLike()),
                        skyStatusEqual(condition.skyStatusEqual()),
                        precipitationTypeEqual(condition.precipitationTypeEqual()),
                        cursorCondition
                )
                .orderBy(
                        feedSorts(condition.sortBy(), condition.sortDirection())
                )
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = false;
        if (contents.size() > pageSize) {
            contents.remove(pageSize);
            hasNext = true;
        }

        return new SliceImpl<>(contents, pageable, hasNext);
    }

    BooleanExpression keywordsContains(String keywordLike) {
        if (keywordLike == null || keywordLike.isEmpty()) return null;
        return feed.content.contains(keywordLike);
    }

    BooleanExpression skyStatusEqual(SkyStatus skyStatus) {
        if (skyStatus == null) return null;
        return feed.weather.skyStatus.eq(skyStatus);
    }

    BooleanExpression precipitationTypeEqual(PrecipitationType precipitationType) {
        if (precipitationType == null) return null;
        return feed.weather.precipitationType.eq(precipitationType);
    }

    private Predicate cursorCondition(String sortBy,
                                      SortDirection sortDirection,
                                      String cursor,
                                      UUID idAfter) {
        if (cursor == null || idAfter == null) return null;
        boolean isDesc = "DESCENDING".equals(sortDirection.name());

        BooleanBuilder builder = new BooleanBuilder();

        if ("createdAt".equals(sortBy)) {
            LocalDateTime date = LocalDateTime.parse(cursor);
            if (isDesc) {
                builder.or(feed.createdAt.lt(date));
                builder.or(feed.createdAt.eq(date).and(feed.id.lt(idAfter)));
            }
            else {
                builder.or(feed.createdAt.gt(date));
                builder.or(feed.createdAt.eq(date).and(feed.id.gt(idAfter)));
            }
        }

        else {
            long count = Long.parseLong(cursor);
            if (isDesc) {
                builder.or(feed.likeCount.lt(count));
                builder.or(feed.likeCount.eq(count).and(feed.id.lt(idAfter)));
            }
            else {
                builder.or(feed.likeCount.gt(count));
                builder.or(feed.likeCount.eq(count).and(feed.id.gt(idAfter)));
            }
        }

        return builder;
    }

    private OrderSpecifier<?>[] feedSorts(String sortBy, SortDirection sortDirection) {

        String order = "createdAt".equals(sortBy) ? "createdAt" : "likeCount";
        Order direction = "DESCENDING".equals(sortDirection.name()) ?
                Order.DESC : Order.ASC;

        OrderSpecifier<?> mainSort = order.equals("createdAt") ?
                new OrderSpecifier<>(direction, feed.createdAt) :
                new OrderSpecifier<>(direction, feed.likeCount);

        OrderSpecifier<?> tieBreaker = new OrderSpecifier<>(direction, feed.id);

        return new OrderSpecifier[]{mainSort, tieBreaker};
    }

    @Override
    public long countTotalElements(FeedSearchCondition condition) {
        Long total = queryFactory
                .select(feed.count())
                .from(feed)
                .where(
                        keywordsContains(condition.keywordLike()),
                        skyStatusEqual(condition.skyStatusEqual()),
                        precipitationTypeEqual(condition.precipitationTypeEqual())
                )
                .fetchOne();

        return Objects.requireNonNullElse(total, 0L);
    }
}
