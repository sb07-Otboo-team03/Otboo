package com.codeit.otboo.domain.like.repository;

import com.codeit.otboo.domain.like.entity.Like;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    boolean existsByFeedIdAndUserId(UUID feedId, UUID userId);

    Optional<Like> findByFeedIdAndUserId(UUID feedId, UUID userId);

    @Query("SELECT l.feed.id FROM Like l WHERE l.user.id = ?1 AND l.feed.id IN ?2")
    Set<UUID> findFeedIdsByUserIdAndFeedIdIn(UUID userId, List<UUID> feedIds);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.feed.id = ?1")
    void deleteAllByFeedId(UUID feedId);
}
