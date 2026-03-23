package com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record ClothesAttributeDefUpdateRequest(
        @NotBlank(message = "ClothesAttributeNameMissingException")
        String name,

        @NotEmpty(message = "CLOTHES_SELECTABLE_VALUE_MISSING")
        List<String> selectableValues
) {
}
