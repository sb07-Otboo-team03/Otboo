package com.codeit.otboo.domain.clothes.management.vo;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;

import java.util.List;

public record ClothesAttributeSelection(
        List<ClothesAttributeValue> selectedValues,
        List<ClothesAttributeValue> allSelectableValues
) {
}
