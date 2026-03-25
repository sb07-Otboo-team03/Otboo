package com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request;

public record ClothesAttributeSearchCondition(
        String sortBy,
        String sortDirection,
        String keywordLike
) {
    public static ClothesAttributeSearchCondition from(ClothesAttributeSearchRequest searchRequest) {
        return new ClothesAttributeSearchCondition(
                searchRequest.sortBy(),
                searchRequest.sortDirection(),
                searchRequest.keywordLike()
        );
    }
}
