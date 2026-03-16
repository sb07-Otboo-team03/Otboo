package com.codeit.otboo.domain.clothes.dto.reqeust;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeResponse;
import com.codeit.otboo.domain.clothes.entity.ClothesType;

import java.util.ArrayList;
import java.util.UUID;

public record ClothesCreateRequest (
    UUID ownerId,
    String name,
    ClothesType type,
    ArrayList<ClothesAttributeResponse> attributes
){}
