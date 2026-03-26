package com.codeit.otboo.domain.feed.service;

import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;

import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.util.UUID;

public interface FeedService {

    FeedResponse createFeed(FeedCreateRequest request);

    CursorResponse<FeedResponse> getAllFeed(FeedSearchRequest request, UUID authorIdEqual);

    FeedResponse updateFeed(UUID id, FeedUpdateRequest request, UUID authorId);

    void deleteFeed(UUID id, UUID authorId);
}
