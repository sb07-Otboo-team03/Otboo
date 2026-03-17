package com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ClothesAttributeDefResponse(
        UUID id,
        String name,
        List<String> selectableValues,
        LocalDateTime createdAt

) {

}
