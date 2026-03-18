package com.codeit.otboo.domain.clothes.management.mapper;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ClothesMapper {
    public ClothesResponse toDto(
            Clothes clothes,
            String imgUrl,
            ArrayList<ClothesAttributeValue> clothesAttributes){
        if (clothes == null) return null;

        return new ClothesResponse(
            clothes.getId(),
                clothes.getOwner().getId(),
                clothes.getName(),
                imgUrl,
                clothes.getType(),
                new ArrayList<>()//todo: 관련 Mapper 작성 이후 내용 변경
        );
    }
}