package com.codeit.otboo.domain.clothes;

import java.util.List;

public record ClothesAttributeDefUpdateRequest(
        String name,
        List<String> selectableValues
) {
}
