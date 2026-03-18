package com.codeit.otboo.domain.follow.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;

    @Override
    @Transactional
    public FollowResponse createFollow(FollowCreateRequest request) {

        throw new UnsupportedOperationException("🚨for Test");
    }

    @Override
    public FollowSummaryResponse getFollowSummary(UUID userId) {

        throw new UnsupportedOperationException("🚨for Test");
    }

    @Override
    public CursorResponse<FollowResponse> getFollowings(UUID followerId, String nameLike, CursorRequest cursorRequest) {

        throw new UnsupportedOperationException("🚨for Test");
    }

    @Override
    public CursorResponse<FollowResponse> getFollowers(UUID followeeId, String nameLike, CursorRequest cursorRequest) {

        throw new UnsupportedOperationException("🚨for Test");
    }

    @Override
    @Transactional
    public void cancelFollow(UUID followId) {

        throw new UnsupportedOperationException("🚨for Test");
    }
}
