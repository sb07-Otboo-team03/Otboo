package com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request;

public record ClothesAttributeSearchRequest(
        String sortBy,
        String sortDirection,
        String keywordLike
) {
}
