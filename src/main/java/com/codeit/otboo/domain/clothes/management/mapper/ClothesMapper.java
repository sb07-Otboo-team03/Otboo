package com.codeit.otboo.domain.clothes.management.mapper;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeWithDefResponse;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ClothesMapper {
    public static ClothesResponse toDto(
            Clothes clothes,
            String imgUrl,
            ArrayList<ClothesAttributeWithDefResponse> clothesAttributes){
        if (clothes == null) return null;

        return new ClothesResponse(
            clothes.getId(),
                clothes.getOwner().getId(),
                clothes.getName(),
                imgUrl,
                clothes.getType(),
                clothesAttributes
        );
    }
}
