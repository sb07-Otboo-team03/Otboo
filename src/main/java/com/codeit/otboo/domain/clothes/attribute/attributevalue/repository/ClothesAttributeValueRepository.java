package com.codeit.otboo.domain.clothes.attribute.attributevalue.repository;

import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClothesAttributeValueRepository extends JpaRepository<ClothesAttributeValue, UUID> {

    List<ClothesAttributeValue> findByAttributeDefIdInAndIsActiveTrue(List<UUID> definitionId);

    List<ClothesAttributeValue> findByAttributeDefId(UUID definitionId);

    Optional<ClothesAttributeValue> findByAttributeDefIdAndSelectableValue(UUID definitionId, String value);
    List<ClothesAttributeValue> findByAttributeDefIdIn(List<UUID> definitionIds);
}
