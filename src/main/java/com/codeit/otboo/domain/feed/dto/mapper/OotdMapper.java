package com.codeit.otboo.domain.feed.dto.mapper;

import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeWithDefResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.feed.dto.response.FeedOotdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OotdMapper {

    private final BinaryContentUrlResolver resolver;

    public FeedOotdResponse toDto(
            Clothes clothes,
            Map<UUID, List<String>> groupingDefinitionSelectable) {
        if (clothes == null) return null;

        return new FeedOotdResponse(
                clothes.getId(),
                clothes.getName(),
                resolver.resolve(clothes.getBinaryContent().getId()),
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