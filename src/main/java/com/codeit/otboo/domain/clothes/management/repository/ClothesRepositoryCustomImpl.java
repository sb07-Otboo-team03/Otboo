package com.codeit.otboo.domain.clothes.management.repository;

import com.codeit.otboo.domain.clothes.management.dto.query.ClothesCursorQuery;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.codeit.otboo.domain.clothes.management.entity.QClothes.clothes;

@Repository
@RequiredArgsConstructor
public class ClothesRepositoryCustomImpl implements ClothesRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Clothes> findMyClothesList(ClothesCursorQuery query) {
        List<Clothes> result = queryFactory
                .selectFrom(clothes)
                .where(
                    clothes.owner.id.eq(query.ownerId()),
                    query.type() == null ? null : clothes.type.eq(query.type()),
                    query.cursor() == null
                            ? null
                            : clothes.createdAt.lt(query.cursor())
                                .or(query.after() == null
                                        ? null
                                        : clothes.createdAt.eq(query.cursor())
                                            .and(clothes.id.lt(query.after()))
                                )
                )
                .orderBy(clothes.createdAt.desc(), clothes.id.desc())
                .limit(query.limit() + 1)
                .fetch();

        boolean hasNext = result.size() > query.limit();
        if (hasNext) {
            result.remove(result.size() - 1);
        }
        return new SliceImpl<>(result, PageRequest.of(0, query.limit()), hasNext);
    }

    @Override
    public long totalCount(UUID ownerId, ClothesType type) {
        return Objects.requireNonNull(
                queryFactory.select(clothes.count())
                .from(clothes)
                .where(
                        clothes.owner.id.eq(ownerId),
                        type == null ? null : clothes.type.eq(type)
                ).fetchOne()
        );
    }
}
