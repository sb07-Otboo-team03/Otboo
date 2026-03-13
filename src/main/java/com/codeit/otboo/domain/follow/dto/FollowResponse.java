package com.codeit.otboo.domain.follow.dto;

import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;

import java.util.UUID;

public record FollowResponse(
        UUID id,
        UserSummaryResponse followee,
        UserSummaryResponse follower
) {}
