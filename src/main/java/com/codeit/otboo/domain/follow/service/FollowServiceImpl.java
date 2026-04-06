package com.codeit.otboo.domain.follow.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.exception.follow.FollowNotFoundException;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
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

        Optional<Follow> optionalFollow = followRepository.findByFollowerIdAndFolloweeId(
            request.followerId(), request.followeeId());

        if (optionalFollow.isPresent()) {
            Follow savedFollow = optionalFollow.get();

            followRepository.updateIsActive(savedFollow.getId(), true);

            return followMapper.toDto(savedFollow);
        }
        else {
            User followee = userRepository.findById(request.followeeId())
                .orElseThrow(() -> new UserNotFoundException(request.followeeId()));

            User follower = userRepository.findById(request.followerId())
                .orElseThrow(() -> new UserNotFoundException(request.followerId()));

            Follow follow = new Follow(follower, followee);
            Follow savedFollow = followRepository.save(follow);

            Notification notification = Notification.builder()
                .title(follower.getProfile().getName() + "님이 나를 팔로우했어요.")
                .content("")
                .level(NotificationLevel.INFO)
                .receiver(followee)
                .build();

            eventPublisher.publishEvent( new FollowSseEvent(List.of(notification)));

            return followMapper.toDto(savedFollow);
        }
    }

    @Override // 팔로우 요약 정보 조회
    public FollowSummaryResponse getFollowSummary(UUID followeeId, OtbooUserDetails userDetails) {

        userRepository.findById(followeeId)
            .orElseThrow(() -> new UserNotFoundException(followeeId));

        UUID myId = userDetails.getUserResponse().id();
        int followerCount = followRepository.countByFollowerIdAndIsActiveTrue(followeeId);
        int followingCount = followRepository.countByFolloweeIdAndIsActiveTrue(followeeId);

        boolean itsMe = false;
        Follow follow = null;

        if (myId.equals(followeeId)) {
            itsMe = true;
        }
        else {
            follow = followRepository.findByFollowerIdAndFolloweeId(myId, followeeId)
                .orElseThrow(() -> new FollowNotFoundException(myId, followeeId));
        }

        FollowSummaryResponse response = null;

        if (itsMe || !follow.isActive()) {
            response = new FollowSummaryResponse(
                followeeId,
                followingCount,
                followerCount,
                false,
                itsMe ? null : follow.getId(),
                false
            );
        }
        else {
            response = new FollowSummaryResponse(
                followeeId,
                followingCount,
                followerCount,
                true,
                follow.getId(),
                true
            );
        }

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
            "createdAt",
            SortDirection.DESCENDING
        );
    }

    @Override
    @Transactional
    public void cancelFollow(UUID followId) {
        followRepository.updateIsActive(followId, false);
    }

}
