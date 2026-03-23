package com.codeit.otboo.domain.follow.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.util.UUID;

public interface FollowService {

    FollowResponse createFollow(FollowCreateRequest request);

    FollowSummaryResponse getFollowSummary(UUID userId, OtbooUserDetails userDetails);

    CursorResponse<FollowResponse> getFollowings(UUID followerId, String nameLike, CursorRequest cursorRequest);

    CursorResponse<FollowResponse> getFollowers(UUID followeeId, String nameLike, CursorRequest cursorRequest);

    void cancelFollow(UUID followId);
}
