package com.codeit.otboo.domain.clothes.management.repository;

import com.codeit.otboo.domain.clothes.management.dto.query.ClothesCursorQuery;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import org.springframework.data.domain.Slice;

public interface ClothesRepositoryCustom {
    Slice<Clothes> findMyClotheList(ClothesCursorQuery query);
}
