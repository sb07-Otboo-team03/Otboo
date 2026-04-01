package com.codeit.otboo.domain.clothes.management.vo;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;

import java.util.List;
import java.util.Set;

public record ClothesAttributeSelection(
        Set<ClothesAttributeValue> selectedValues,
        List<ClothesAttributeValue> allSelectableValues
) {
}
