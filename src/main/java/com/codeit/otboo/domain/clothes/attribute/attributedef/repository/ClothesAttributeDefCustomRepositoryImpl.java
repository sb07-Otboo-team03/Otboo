package com.codeit.otboo.domain.clothes.attribute.attributedef.repository;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeSearchCondition;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.codeit.otboo.domain.clothes.attribute.attributedef.entity.QClothesAttributeDef.clothesAttributeDef;
import static com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.QClothesAttributeValue.clothesAttributeValue;

@RequiredArgsConstructor
public class ClothesAttributeDefCustomRepositoryImpl implements  ClothesAttributeDefCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ClothesAttributeDef> searchAttributes(
            ClothesAttributeSearchCondition clothesAttributeSearchCondition) {
        return queryFactory.select(clothesAttributeDef)
                .from(clothesAttributeDef)
                .leftJoin(clothesAttributeValue)
                .on(clothesAttributeValue.attributeDef.eq(clothesAttributeDef)
                        .and(clothesAttributeValue.isActive.eq(true)))
                .where(
                        keywordLike(clothesAttributeSearchCondition.keywordLike())
                )
                .distinct()
                .orderBy(getSortOrder(
                        clothesAttributeSearchCondition.sortBy(),
                        clothesAttributeSearchCondition.sortDirection()))
                .fetch();
    }

    private BooleanExpression keywordLike(String keyword) {
        if(!StringUtils.hasText(keyword)){
            return null;
        }
        return clothesAttributeDef.name.containsIgnoreCase(keyword)
                .or(clothesAttributeValue.selectableValue.containsIgnoreCase(keyword)
                        .and(clothesAttributeValue.isActive.eq(true)));
    }

    private OrderSpecifier<?> getSortOrder(String sortBy, String sortDirection) {
        Order order = (sortDirection != null && sortDirection.toUpperCase().startsWith("ASC"))
                ? Order.ASC : Order.DESC;

        ComparableExpressionBase<?> field;
        if("name".equalsIgnoreCase(sortBy)){
            field = clothesAttributeDef.name;
        } else {
            field = clothesAttributeDef.createdAt;
        }

        return new OrderSpecifier<>(order, field);
    }
}
