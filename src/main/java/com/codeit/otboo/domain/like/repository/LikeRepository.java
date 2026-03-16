package com.codeit.otboo.domain.like.repository;

import com.codeit.otboo.domain.like.entity.Like;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, UUID> {

}
