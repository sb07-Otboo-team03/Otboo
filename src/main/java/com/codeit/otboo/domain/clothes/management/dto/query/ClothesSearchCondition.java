package com.codeit.otboo.domain.clothes.management.dto.query;

import com.codeit.otboo.domain.clothes.management.entity.ClothesType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClothesSearchCondition(
    LocalDateTime cursor,
    UUID after,
    Integer limit,
    ClothesType type,
    UUID ownerId
){}
