package com.codeit.otboo.domain.clothes.management.repository;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClothesRepository extends JpaRepository<Clothes, UUID>, ClothesRepositoryCustom{
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
}
