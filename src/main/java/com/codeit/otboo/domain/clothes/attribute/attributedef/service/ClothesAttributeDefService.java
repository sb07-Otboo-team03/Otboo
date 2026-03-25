package com.codeit.otboo.domain.clothes.attribute.attributedef.service;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeSearchCondition;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeDefResponse;

import java.util.List;
import java.util.UUID;

public interface ClothesAttributeDefService {

    ClothesAttributeDefResponse createAttributeDef(ClothesAttributeDefCreateRequest request);

    ClothesAttributeDefResponse updateAttributeDef(UUID definition_id, ClothesAttributeDefUpdateRequest request);

    void deleteAttributeDef(UUID definition_id);

    List<ClothesAttributeDefResponse> getAllAttributeDef( ClothesAttributeSearchCondition searchCondition);

}