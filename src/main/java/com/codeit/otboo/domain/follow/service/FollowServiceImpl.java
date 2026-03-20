package com.codeit.otboo.domain.follow.service;

import static java.util.Arrays.stream;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UserRepository userRepository;
    private final FollowMapper followMapper;
    private final AuthenticationManager authenticationManager;


    private LocalDateTime toLocalDateTime(String cursor) {
        return (cursor == null) ? null :LocalDateTime.parse(cursor);
    }
    
    @Override
    @Transactional
    public FollowResponse createFollow(FollowCreateRequest request) {

        followRepository.findByFollowerIdAndFolloweeId(request.followerId(),
                request.followeeId())
            .ifPresent(follow -> {
                throw new IllegalArgumentException("🚨");
            });

        User follower = userRepository.findById(request.followerId())
            .orElseThrow(() -> new IllegalArgumentException("🚨follower err"));

        User followee = userRepository.findById(request.followeeId())
            .orElseThrow(() -> new IllegalArgumentException("🚨followee err"));

        Follow follow = new Follow(follower, followee);
        Follow saveFollow = followRepository.save(follow);

        return followMapper.toDto(saveFollow);
    }

    @Override
    public FollowSummaryResponse getFollowSummary(UUID userId,OtbooUserDetails userDetails) {

        userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("🚨userId err"));

        UserResponse userResponse = userDetails.getUserResponse();

        int followerCount = followRepository.countByFolloweeId(userId);
        int followingCount = followRepository.countByFollowerId(userId);
        boolean followedByMe = followRepository.existsByFollowerIdAndFolloweeId(userId, userResponse.id());
        boolean followingMe = followRepository.existsByFollowerIdAndFolloweeId(userResponse.id(), userId);

        return new FollowSummaryResponse(
            userId,
            followerCount,
            followingCount,
            followedByMe,
            (followingMe ? userId : null),
            followingMe
        );
    }

    @Override
    public CursorResponse<FollowResponse> getFollowings(UUID followId, String nameLike, CursorRequest cursorRequest) {
        return getFollows(false, followId, nameLike, cursorRequest);
    }

    @Override
    public CursorResponse<FollowResponse> getFollowers(UUID followId, String nameLike, CursorRequest cursorRequest) {
        return getFollows(true, followId, nameLike, cursorRequest);
    }

    @Override
    @Transactional
    public void cancelFollow(UUID followId) {

        Follow follow = followRepository.findById(followId)
            .orElseThrow(() -> new IllegalArgumentException("🚨"));

        followRepository.delete(follow);
    }

    public CursorResponse<FollowResponse> getFollows(Boolean isFollower, UUID followId, String nameLike, CursorRequest cursorRequest) {

        LocalDateTime cursor = toLocalDateTime(cursorRequest.cursor());
        Pageable pageable = PageRequest.of(0, cursorRequest.limit() + 1);

        List<FollowDto> results = isFollower
            ? followRepository.findAllFollowers(followId, nameLike, cursor, cursorRequest.idAfter(), pageable)
            : followRepository.findAllFollowings(followId, nameLike, cursor, cursorRequest.idAfter(), pageable);

        boolean hasNext = results.size() > cursorRequest.limit();

        List<FollowDto> page = hasNext
            ? results.subList(0, cursorRequest.limit())
            : results;

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (!page.isEmpty()) {
            FollowDto last = page.get(page.size() - 1);
            nextCursor = last.createdAt().toString();
            nextIdAfter = last.id();
        }

        List<FollowResponse> content = page.stream()
            .map(followMapper::toDto)
            .toList();

        return CursorResponse.fromList(
            content,
            nextCursor,
            nextIdAfter,
            hasNext,
            "createdAt",
            SortDirection.DESCENDING
        );
    }
}
