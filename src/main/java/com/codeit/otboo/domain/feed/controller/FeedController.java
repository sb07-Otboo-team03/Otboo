package com.codeit.otboo.domain.feed.controller;

import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedResponse;
import com.codeit.otboo.domain.feed.service.FeedService;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @PostMapping
    public ResponseEntity<FeedResponse> createFeed(@Valid @RequestBody FeedCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(feedService.createFeed(request));
    }

    @GetMapping
    public ResponseEntity<CursorResponse<FeedResponse>> getFeedList(
            @ParameterObject @ModelAttribute @Valid FeedSearchRequest request,
            @AuthenticationPrincipal OtbooUserDetails details
            ) {
        UUID userId = details.getUserResponse().id();
        return ResponseEntity.ok(feedService.getAllFeed(request, userId));
    }

    @PatchMapping("/{feedId}")
    public ResponseEntity<FeedResponse> updateFeed (@Valid @RequestBody FeedUpdateRequest request,
                                                    @PathVariable UUID feedId,
                                                    @AuthenticationPrincipal OtbooUserDetails details) {
        UUID authorId = details.getUserResponse().id();
        return ResponseEntity.ok(feedService.updateFeed(feedId, request, authorId));
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId,
                                           @AuthenticationPrincipal OtbooUserDetails details) {
        UUID authorId = details.getUserResponse().id();
        feedService.deleteFeed(feedId, authorId);
        return ResponseEntity.noContent().build();
    }
}
