package com.codeit.otboo.domain.like.controller;

import com.codeit.otboo.domain.like.service.LikeService;
import com.codeit.otboo.global.security.OtbooUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/feeds/{feedId}/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<Void> like(@PathVariable("feedId") UUID feedId,
                                     @AuthenticationPrincipal OtbooUserDetails details) {
        UUID userId = details.getUserResponse().id();
        likeService.feedLike(feedId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unlike(@PathVariable("feedId") UUID feedId,
                                       @AuthenticationPrincipal OtbooUserDetails details) {
        UUID userId = details.getUserResponse().id();
        likeService.feedUnlike(feedId, userId);
        return ResponseEntity.noContent().build();
    }
}
