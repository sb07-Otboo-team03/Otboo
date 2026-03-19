package com.codeit.otboo.domain.clothes.management.dto.request;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.request.ClothesAttributeRequest;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public record ClothesCreateRequest (
    @NotNull
    UUID ownerId,
    @NotBlank
    String name,
    @NotNull
    ClothesType type,
    ArrayList<ClothesAttributeRequest> attributes
){}