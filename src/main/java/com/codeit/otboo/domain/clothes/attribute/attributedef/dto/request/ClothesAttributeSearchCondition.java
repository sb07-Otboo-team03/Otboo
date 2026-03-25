package com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request;

public record ClothesAttributeSearchCondition(
        String sortBy,
        String sortDirection,
        String keywordLike
) {
}
