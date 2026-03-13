package com.codeit.otboo.domain.clothes;

import java.util.List;

public record ClothesAttributeDefCreateRequest(
        String name,
        List<String> selectableValues
) {

}
