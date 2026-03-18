package com.codeit.otboo.domain.follow.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FollowCreateRequest(
        @NotNull(message = "followee ID는 필수입니다")
        UUID followeeId,

        @NotNull(message = "follower ID는 필수입니다")
        UUID followerId
) {}
