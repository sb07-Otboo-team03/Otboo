package com.codeit.otboo.domain.clothes.dto.reqeust;

import java.util.List;

public record ClothesAttributeDefUpdateRequest(
        String name,
        List<String> selectableValues
) {
}
