package com.codeit.otboo.domain.clothes.attribute.attributedef.mapper;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeDefResponse;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClothesAttributeDefMapper {

    public ClothesAttributeDef toClothesAttributeDef(ClothesAttributeDefCreateRequest request) {
        return ClothesAttributeDef.builder()
                .name(request.name())
                .build();
    }

    public ClothesAttributeDefResponse toClothesAttributeDefResponse(
            ClothesAttributeDef clothesAttributeDef,
            List<String> selectableValues) {
        return ClothesAttributeDefResponse.builder()
                .id(clothesAttributeDef.getId())
                .name(clothesAttributeDef.getName())
                .selectableValues(selectableValues)
                .createdAt(clothesAttributeDef.getCreatedAt())
                .build();
    }

}
