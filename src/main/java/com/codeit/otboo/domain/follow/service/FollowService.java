package com.codeit.otboo.domain.follow.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.global.slice.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;

public interface FollowService {

    FollowResponse createFollow(@Valid FollowCreateRequest request);

    FollowSummaryResponse getFollowSummary(UUID userId);

    PageResponse<FollowResponse> getFollowings(UUID followerId, String nameLike, CursorRequest cursorRequest);

    PageResponse<FollowResponse> getFollowers(UUID followeeId, String nameLike, CursorRequest cursorRequest);

    void cancelFollow(UUID followId);
}
