package com.codeit.otboo.domain.feed.repository;

import com.codeit.otboo.domain.feed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID>, FeedRepositoryCustom {

    List<Feed> findByUpdatedAtAfter(LocalDateTime updatedAt);
}
