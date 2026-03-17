package com.codeit.otboo.domain.follow.controller;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryResponse;
import com.codeit.otboo.domain.follow.service.FollowService;
import com.codeit.otboo.global.slice.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController {
    private final FollowService followService;

    //팔로우 생성
    @PostMapping
    public ResponseEntity<FollowResponse> createFollow(@Valid @RequestBody FollowCreateRequest request) {

        FollowResponse response = followService.createFollow(request);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    // 팔로우 요약 정보 조회
    @GetMapping("/summary")
    public ResponseEntity<FollowSummaryResponse> getFollowSummary(@RequestParam UUID userId) {

        FollowSummaryResponse response = followService.getFollowSummary(userId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    // 팔로잉 목록 조회
    @GetMapping("/followings")
    public ResponseEntity<PageResponse<FollowResponse>> getFollowings(
        @RequestParam UUID followerId,
        @RequestParam(required = false) String nameLike,
        @Valid CursorRequest cursorRequest
    ) {

        PageResponse<FollowResponse> response = followService.getFollowings(followerId, nameLike, cursorRequest);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    // 팔로워 목록 조회
    @GetMapping("/followers")
    public ResponseEntity<PageResponse<FollowResponse>> getFollowers(
        @RequestParam UUID followeeId,
        @RequestParam(required = false) String nameLike,
        @Valid CursorRequest cursorRequest
    ) {

        PageResponse<FollowResponse> response = followService.getFollowers(followeeId, nameLike, cursorRequest);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    // 팔로우 취소
    @DeleteMapping("/{followId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> cancelFollow(@PathVariable UUID followId) {

        followService.cancelFollow(followId);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }
}
