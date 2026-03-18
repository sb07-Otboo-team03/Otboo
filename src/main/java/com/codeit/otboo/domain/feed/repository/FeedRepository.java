package com.codeit.otboo.domain.feed.repository;

import com.codeit.otboo.domain.feed.entity.Feed;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID>, FeedRepositoryCustom {

}
