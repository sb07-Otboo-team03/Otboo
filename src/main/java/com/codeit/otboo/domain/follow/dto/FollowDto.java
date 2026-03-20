package com.codeit.otboo.domain.follow.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FollowDto (
    UUID id,
    LocalDateTime createdAt,
    UUID followerId,
    String followerName,
    UUID followerProfileImageId,
    UUID followeeId,
    String followeeName,
    UUID followeeProfileImageId
) {
}
