package com.codeit.otboo.domain.clothes.management.vo;

import java.util.UUID;

public record ClothesAttributeValueKey(
        UUID definitionId,
        String value
) {
}
