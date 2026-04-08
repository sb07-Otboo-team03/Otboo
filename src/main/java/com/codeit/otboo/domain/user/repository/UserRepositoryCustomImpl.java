package com.codeit.otboo.domain.user.repository;

import com.codeit.otboo.domain.user.dto.request.UserSearchCondition;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.slice.dto.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
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

import static com.codeit.otboo.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryCustomImpl implements UserRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<User> findAllByKeywordLike(UserSearchCondition condition) {
        int pageSize = condition.limit();

        Pageable pageable = PageRequest.of(0, pageSize);

        Predicate cursorCondition = cursorCondition(
                condition.sortBy(),
                condition.sortDirection(),
                condition.cursor(),
                condition.idAfter()
        );

        List<User> users = queryFactory
                .selectFrom(user)
                .where(
                        keywordsContains(condition.emailLike()),
                        roleEqual(condition.roleEqual()),
                        lockedEqual(condition.locked()),
                        cursorCondition
                )
                .orderBy(
                        userSorts(condition.sortBy(), condition.sortDirection())
                )
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = false;
        if (users.size() > pageSize) {
            users.remove(pageSize);
            hasNext = true;
        }

        return new SliceImpl<>(users, pageable, hasNext);
    }

    private Predicate cursorCondition(String sortBy,
                                      SortDirection sortDirection,
                                      String cursor,
                                      UUID idAfter) {

        if(cursor == null || idAfter == null) return null;
        boolean isDesc = "DESCENDING".equals(sortDirection.name());

        BooleanBuilder builder = new BooleanBuilder();

        if("createdAt".equals(sortBy)) {
            LocalDateTime date = LocalDateTime.parse(cursor);
            if (isDesc) {
                builder.or(user.createdAt.lt(date));
                builder.or(user.createdAt.eq(date).and(user.id.lt(idAfter)));
            }
            else {
                builder.or(user.createdAt.gt(date));
                builder.or(user.createdAt.eq(date).and(user.id.gt(idAfter)));
            }
        } else {
            if (isDesc) {
                builder.or(user.email.lt(cursor));
                builder.or(
                        user.email.eq(cursor)
                                .and(user.id.lt(idAfter))
                );
            } else {
                builder.or(user.email.gt(cursor));
                builder.or(
                        user.email.eq(cursor)
                                .and(user.id.gt(idAfter))
                );
            }
        }
        return builder;
    }

    private Predicate keywordsContains(String keywordLike) {
        if (keywordLike == null || keywordLike.isEmpty()) return null;
        return user.email.contains(keywordLike);
    }
    private Predicate roleEqual(Role role) {
        if (role == null) return null;
        return user.role.eq(role);
    }

    private Predicate lockedEqual(Boolean locked) {
        log.debug("lockedEqual: {}", locked);
        if (locked == null) return null;
        return user.locked.eq(locked);
    }

    private OrderSpecifier<?>[] userSorts(String sortBy, SortDirection sortDirection) {

        String order = "createdAt".equals(sortBy) ? "createdAt" : "email";
        Order direction = "DESCENDING".equals(sortDirection.name()) ?
                Order.DESC : Order.ASC;

        OrderSpecifier<?> mainSort = order.equals("createdAt") ?
                new OrderSpecifier<>(direction, user.createdAt) :
                new OrderSpecifier<>(direction, user.email);

        OrderSpecifier<?> tieBreaker = new OrderSpecifier<>(direction, user.id);

        return new OrderSpecifier[]{mainSort, tieBreaker};
    }

    @Override
    public long countTotalElements(UserSearchCondition condition) {
        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        keywordsContains(condition.emailLike()),
                        roleEqual(condition.roleEqual()),
                        lockedEqual(condition.locked())
                )
                .fetchOne();

        return Objects.requireNonNullElse(total, 0L);
    }

}
