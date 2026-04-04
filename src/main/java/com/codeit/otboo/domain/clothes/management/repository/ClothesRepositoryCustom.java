package com.codeit.otboo.domain.clothes.management.repository;

import com.codeit.otboo.domain.clothes.management.dto.query.ClothesCursorQuery;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface ClothesRepositoryCustom {
    Slice<Clothes> findMyClotheList(ClothesCursorQuery query);

    Long totalCount(UUID ownerId, ClothesType type);
}
