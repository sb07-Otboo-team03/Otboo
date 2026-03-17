package com.codeit.otboo.domain.clothes.management.dto.request;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.request.ClothesAttributeRequest;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;

import java.util.ArrayList;
import java.util.UUID;

public record ClothesCreateRequest (
    UUID ownerId,
    String name,
    ClothesType type,
    ArrayList<ClothesAttributeRequest> attributes
){}
