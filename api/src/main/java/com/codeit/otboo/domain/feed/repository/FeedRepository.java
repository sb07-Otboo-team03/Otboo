package com.codeit.otboo.domain.feed.repository;

import com.codeit.otboo.domain.feed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID>, FeedRepositoryCustom {

    List<Feed> findByUpdatedAtAfter(LocalDateTime updatedAt);

    @Query("SELECT f.commentCount FROM Feed f WHERE f.id = ?1")
    Optional<Integer> findCommentCountByFeedId(UUID feedId);
}
