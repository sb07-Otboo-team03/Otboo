package com.codeit.otboo.domain.clothes.dto.response;

import java.util.List;
import java.util.UUID;

public record ClothesAttributeWithDefResponse (
    UUID definitionId,
    String definitionName,
    List<String> selectableValue,
    String value
){}
