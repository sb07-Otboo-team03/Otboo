package com.codeit.otboo.domain.comment.controller;

import com.codeit.otboo.domain.comment.dto.CommentCreateRequest;
import com.codeit.otboo.domain.comment.dto.CommentResponse;
import com.codeit.otboo.domain.comment.service.CommentService;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/feeds/{feedId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable("feedId") UUID feedId,
                                                         @AuthenticationPrincipal OtbooUserDetails details,
                                                         @Valid @RequestBody CommentCreateRequest request) {
        UUID userId = details.getUserResponse().id();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(feedId, userId, request));
    }

    @GetMapping
    public ResponseEntity<CursorResponse<CommentResponse>> getAllComments(@PathVariable("feedId") UUID feedId,
                                                                          @RequestParam(required = false) String cursor,
                                                                          @RequestParam(required = false) UUID idAfter,
                                                                          @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(commentService.getAllComments(feedId, cursor, idAfter, limit));
    }
}
