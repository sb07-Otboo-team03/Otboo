package com.codeit.otboo.domain.comment.repository;

import com.codeit.otboo.domain.comment.entity.Comment;
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
import java.util.UUID;

import static com.codeit.otboo.domain.comment.entity.QComment.comment;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Comment> findAllByCursor(UUID feedId, String cursor, UUID idAfter, int limit) {

        Pageable pageable = PageRequest.of(0, limit);

        List<Comment> contents = queryFactory
                .selectFrom(comment)
                .where(
                        comment.feed.id.eq(feedId),
                        cursorCondition(cursor, idAfter)
                )
                .orderBy(comment.createdAt.desc(), comment.id.desc())
                .limit(limit + 1)
                .fetch();

        boolean hasNext = false;
        if (contents.size() > limit) {
            contents.remove(limit);
            hasNext = true;
        }

        return new SliceImpl<>(contents, pageable, hasNext);
    }

    BooleanExpression cursorCondition(String cursor, UUID idAfter) {
        if (cursor == null || idAfter == null) return null;

        LocalDateTime date = LocalDateTime.parse(cursor);
        return comment.createdAt.lt(date)
                .or(comment.createdAt.eq(date).and(comment.id.lt(idAfter)));
    }
}
