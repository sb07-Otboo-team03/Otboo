package com.codeit.otboo.domain.clothes.attribute.attributedef.repository;

import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClothesAttributeDefRepository extends
        JpaRepository<ClothesAttributeDef, UUID>, ClothesAttributeDefCustomRepository {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
