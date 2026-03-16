package com.codeit.otboo.domain.clothes.dto.reqeust;

import java.util.List;

public record ClothesAttributeDefCreateRequest(
        String name,
        List<String> selectableValues
) {

}
