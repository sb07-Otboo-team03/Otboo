package com.codeit.otboo.domain.clothes.attribute.attributedef.repository;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeSearchCondition;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;

import java.util.List;

public interface ClothesAttributeDefCustomRepository {
    List<ClothesAttributeDef> searchAttributes(ClothesAttributeSearchCondition clothesAttributeSearchCondition);
}
