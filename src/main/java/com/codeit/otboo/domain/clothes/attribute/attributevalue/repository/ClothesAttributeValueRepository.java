package com.codeit.otboo.domain.clothes.attribute.attributevalue.repository;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClothesAttributeValueRepository extends JpaRepository<ClothesAttributeValue, UUID> {

    List<ClothesAttributeValue> findByAttributeDefIdAndIsActiveTrue(UUID id);

    List<ClothesAttributeValue> findByAttributeDefId(UUID definitionId);
}
