package com.codeit.otboo.domain.follow.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.exception.follow.DuplicateFollowException;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.sse.event.FollowSseEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;
    private final ApplicationEventPublisher eventPublisher;

    private LocalDateTime toLocalDateTime(String cursor) {

        return (cursor == null) ? null :LocalDateTime.parse(cursor);
    }

    @Override
    @Transactional
    public FollowResponse create(FollowCreateRequest request) {

        Optional<Follow> byFollowerIdAndFolloweeId = followRepository.findByFollowerIdAndFolloweeId(
            request.followerId(), request.followeeId());

        if(byFollowerIdAndFolloweeId.isPresent()) {
            throw new DuplicateFollowException(request.followerId(), request.followeeId());
        }

        User followee = userRepository.findById(request.followeeId())
            .orElseThrow(() -> new UserNotFoundException(request.followeeId()));

        User follower = userRepository.findById(request.followerId())
            .orElseThrow(() -> new UserNotFoundException(request.followerId()));

        Follow follow = new Follow(follower, followee);
        Follow savedFollow = followRepository.save(follow);

        String title = follower.getProfile().getName() + "님이 나를 팔로우했어요.";
        eventPublisher.publishEvent(new FollowSseEvent(title, "", followee.getId()));

        return followMapper.toDto(savedFollow);
    }

    @Override // 팔로우 요약 정보 조회
    public FollowSummaryResponse getFollowSummary(UUID followeeId, UUID myId) {

        userRepository.findById(followeeId)
            .orElseThrow(() -> new UserNotFoundException(followeeId));

        int followingCount = followRepository.countByFollowerId(followeeId);
        int followerCount = followRepository.countByFolloweeId(followeeId);

        Optional<Follow> follow = followRepository.findByFollowerIdAndFolloweeId(myId, followeeId);
        boolean isEmpty = follow.isEmpty();

        FollowSummaryResponse response = new FollowSummaryResponse(
            followeeId,
            followerCount,
            followingCount,
            !isEmpty,
            isEmpty ? null : follow.get().getId(),
            !isEmpty
        );

        return response;
    }

    @Override
    public CursorResponse<FollowResponse> getFollowings(
        UUID followId,
        String nameLike,
        CursorRequest cursorRequest
    ) {
        return getFollows(false, followId, nameLike, cursorRequest);
    }

    @Override
    public CursorResponse<FollowResponse> getFollowers(
        UUID followId,
        String nameLike,
        CursorRequest cursorRequest
    ) {
        return getFollows(true, followId, nameLike, cursorRequest);
    }

    public CursorResponse<FollowResponse> getFollows(
        Boolean isFollower,
        UUID followId,
        String nameLike,
        CursorRequest cursorRequest
    ) {
        LocalDateTime cursor = toLocalDateTime(cursorRequest.cursor());
        Pageable pageable = PageRequest.of(0, cursorRequest.limit() + 1);

        String safeNameLike = (nameLike == null || nameLike.isBlank())
            ? ""
            : nameLike;

        List<FollowDto> results = isFollower
            ? followRepository.findAllFollowers(followId, safeNameLike, cursor, cursorRequest.idAfter(), pageable)
            : followRepository.findAllFollowings(followId, safeNameLike, cursor, cursorRequest.idAfter(), pageable);

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
            null,
            "createdAt",
            SortDirection.DESCENDING
        );
    }

    @Override
    @Transactional
    public void cancelFollow(UUID followId) {
        followRepository.deleteById(followId);
    }

}
