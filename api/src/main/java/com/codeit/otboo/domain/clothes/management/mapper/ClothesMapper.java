package com.codeit.otboo.domain.clothes.management.mapper;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeWithDefResponse;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClothesMapper {
    public ClothesResponse toDto(
            Clothes clothes,
            String imageUrl,
            Map<UUID, List<String>> groupingDefinitionSelectable){
        if (clothes == null) return null;

        return new ClothesResponse(
            clothes.getId(),
                clothes.getOwner().getId(),
                clothes.getName(),
                imageUrl,
                clothes.getType(),
                clothes.getValues().stream().map(clothesAttributeValue ->
                        new ClothesAttributeWithDefResponse(
                                clothesAttributeValue.getAttributeDef().getId(),
                                clothesAttributeValue.getAttributeDef().getName(),
                                groupingDefinitionSelectable.get(
                                        clothesAttributeValue.getAttributeDef().getId()
                                ),
                                clothesAttributeValue.getSelectableValue()
                        )
                ).toList()
        );
    }
}