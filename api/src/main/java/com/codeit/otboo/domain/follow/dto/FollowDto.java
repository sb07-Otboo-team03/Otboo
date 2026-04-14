package com.codeit.otboo.domain.follow.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FollowDto (
    UUID id,
    LocalDateTime createdAt,
    UUID followeeId,
    String followeeName,
    UUID followeeProfileImageId,
    UUID followerId,
    String followerName,
    UUID followerProfileImageId
) {
}
