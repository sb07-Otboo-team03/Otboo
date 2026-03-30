package com.codeit.otboo.domain.user.repository;

import com.codeit.otboo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String userEmail);

    boolean existsByEmail(String email);
}
