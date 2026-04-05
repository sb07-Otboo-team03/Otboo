package com.codeit.otboo.domain.follow.repository;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.entity.Follow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    @Modifying
    @Query("UPDATE Follow f SET f.isActive = :isActive WHERE f.id = :id")
    void updateIsActive(@Param("id") UUID id, @Param("isActive") boolean isActive);

    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    int countByFollowerIdAndIsActiveTrue(UUID followerId);
    int countByFolloweeIdAndIsActiveTrue(UUID followeeId);

    @Query("SELECT f.follower.id FROM Follow f WHERE f.followee.id = :followeeId AND f.isActive = true")
    Set<UUID> findAllFollowerIdsByFolloweeIdAndIsActiveTrue(@Param("followeeId") UUID followeeId);

    @Query("""
       SELECT new com.codeit.otboo.domain.follow.dto.FollowDto(
            f.id,
            f.createdAt,
            followee.id,
            pfollowee.name,
            bpfollowee.id,
            follower.id,
            pfollower.name,
            bpfollower.id)
       FROM Follow f
            JOIN f.followee followee
            JOIN f.follower follower
            JOIN followee.profile pfollowee
            JOIN follower.profile pfollower
            LEFT JOIN pfollowee.binaryContent bpfollowee
            LEFT JOIN pfollower.binaryContent bpfollower
       WHERE follower.id = :followId
           AND LOWER(pfollower.name) LIKE LOWER(CONCAT('%', :nameLike, '%'))
           AND (
               CAST(:cursor AS timestamp) IS NULL
               OR (
                   f.createdAt < :cursor
                   OR (
                       f.createdAt = :cursor
                      AND (:idAfter IS NULL OR f.id < :idAfter)
                   )
               )
          )
          AND f.isActive = true
       ORDER BY f.createdAt DESC, f.id DESC
    """)
    List<FollowDto> findAllFollowings(
        @Param("followId") UUID followId,
        @Param("nameLike") String nameLike,
        @Param("cursor") LocalDateTime cursor,
        @Param("idAfter") UUID idAfter,
        Pageable pageable
    );


    @Query("""
       SELECT new com.codeit.otboo.domain.follow.dto.FollowDto(
            f.id,
            f.createdAt,
            followee.id,
            pfollowee.name,
            bpfollowee.id,
            follower.id,
            pfollower.name,
            bpfollower.id)
       FROM Follow f
            JOIN f.followee followee
            JOIN f.follower follower
            JOIN followee.profile pfollowee
            JOIN follower.profile pfollower
            LEFT JOIN pfollowee.binaryContent bpfollowee
            LEFT JOIN pfollower.binaryContent bpfollower
       WHERE followee.id = :followId
           AND LOWER(pfollowee.name) LIKE LOWER(CONCAT('%', :nameLike, '%'))
           AND (
               CAST(:cursor AS timestamp) IS NULL
               OR (
                   f.createdAt < :cursor
                   OR (
                       f.createdAt = :cursor
                      AND (:idAfter IS NULL OR f.id < :idAfter)
                   )
               )
          )
          AND f.isActive = true
       ORDER BY f.createdAt DESC, f.id DESC
    """)
    List<FollowDto> findAllFollowers(
        @Param("followId") UUID followId,
        @Param("nameLike") String nameLike,
        @Param("cursor") LocalDateTime cursor,
        @Param("idAfter") UUID idAfter,
        Pageable pageable
    );
}
