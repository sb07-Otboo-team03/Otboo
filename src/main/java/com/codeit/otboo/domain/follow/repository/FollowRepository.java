package com.codeit.otboo.domain.follow.repository;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.antlr.v4.runtime.atn.SemanticContext.OR;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    int countByFollowerId(UUID followerId);
    int countByFolloweeId(UUID followeeId);
    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    @Query("""
       SELECT new com.codeit.otboo.domain.follow.dto.FollowDto(
            f.id,
            f.createdAt,
            rp.id,
            rp.name,
            rpb.id,
            ep.id,
            ep.name,
            epb.id)
       FROM Follow f
            JOIN f.follower r
            JOIN f.followee e
            JOIN r.profile rp
            JOIN e.profile ep
            LEFT JOIN rp.binaryContent rpb
            LEFT JOIN ep.binaryContent epb
       WHERE e.id = :followeeId
           AND ep.name = '%:nameLike%'
           AND ( :cursor IS NULL
               OR f.createdAt < :cursor
               OR (f.createdAt = :cursor
                 AND (:idAfter IS NULL OR f.id < :idAfter)
               )
           )
       ORDER BY f.createdAt DESC, f.id DESC
    """)
    List<FollowDto> findAllFollowings(
        @Param("followeeId") UUID followeeId,
        @Param("nameLike") String nameLike,
        @Param("cursor")
        LocalDateTime cursor,
        @Param("idAfter") UUID idAfter,
        Pageable pageable
    );


    @Query("""
       SELECT new com.codeit.otboo.domain.follow.dto.FollowDto(
            f.id,
            f.createdAt,
            rp.id,
            rp.name,
            rpb.id,
            ep.id,
            ep.name,
            epb.id)
       FROM Follow f
            JOIN f.follower r
            JOIN f.followee e
            JOIN r.profile rp
            JOIN e.profile ep
            LEFT JOIN rp.binaryContent rpb
            LEFT JOIN ep.binaryContent epb
       WHERE r.id = :followerId
           AND rp.name = '%:nameLike%'
           AND ( :cursor IS NULL
               OR f.createdAt < :cursor
               OR (f.createdAt = :cursor
                  AND(:idAfter IS NULL OR f.id < :idAfter)
               )
          )
       ORDER BY f.createdAt DESC, f.id DESC
    """)
    List<FollowDto> findAllFollowers(
        @Param("followerId") UUID followerId,
        @Param("nameLike") String nameLike,
        @Param("cursor")
        LocalDateTime cursor,
        @Param("idAfter") UUID idAfter,
        Pageable pageable
    );
}
