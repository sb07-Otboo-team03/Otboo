package com.codeit.otboo.domain.follow.repository;

import com.codeit.otboo.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
}
