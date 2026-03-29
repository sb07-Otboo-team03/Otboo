package com.codeit.otboo.domain.comment.service;

import com.codeit.otboo.domain.comment.dto.CommentCreateRequest;
import com.codeit.otboo.domain.comment.dto.CommentResponse;
import com.codeit.otboo.global.slice.dto.CursorResponse;

import java.util.UUID;

public interface CommentService {

    CommentResponse createComment(UUID feedId, UUID userId, CommentCreateRequest request);

    CursorResponse<CommentResponse> getAllComments(UUID feedId, String cursor, UUID idAfter, int limit);
}
