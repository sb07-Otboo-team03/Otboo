package com.codeit.otboo.domain.clothes.attribute.attributevalue.mapper;

import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.response.ClothesAttributeResponse;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import org.springframework.stereotype.Component;

@Component
public class ClothesAttributeValueMapper {

    public ClothesAttributeValue toClothesAttributeValue(
            ClothesAttributeResponse clothesAttributeResponse,
            ClothesAttributeDef clothesAttributeDef) {
        return ClothesAttributeValue.builder()
                .selectableValue(clothesAttributeResponse.value())
                .attributeDef(clothesAttributeDef)
                .isActive(true)
                .build();
    }

    public ClothesAttributeValue toClothesAttributeValue(
            String value,
            ClothesAttributeDef clothesAttributeDef) {
        return ClothesAttributeValue.builder()
                .selectableValue(value)
                .attributeDef(clothesAttributeDef)
                .isActive(true)
                .build();
    }

    public ClothesAttributeResponse toClothesAttributeResponse(ClothesAttributeValue clothesAttributeValue) {
        return ClothesAttributeResponse.builder()
                .definitionId(clothesAttributeValue.getAttributeDef().getId())
                .value(clothesAttributeValue.getSelectableValue())
                .build();
    }
}
