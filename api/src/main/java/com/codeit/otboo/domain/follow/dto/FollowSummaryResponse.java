package com.codeit.otboo.domain.follow.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record FollowSummaryResponse(
        UUID followeeId,
        int followerCount,
        int followingCount,
        boolean followedByMe,
        UUID followedByMeId,
        boolean followingMe
) {
}
