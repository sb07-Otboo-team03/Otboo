package com.codeit.otboo.domain.user.repository;

import com.codeit.otboo.domain.user.entity.TemporaryPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TemporaryPasswordRepository extends JpaRepository<TemporaryPassword, UUID> {

    Optional<TemporaryPassword> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}