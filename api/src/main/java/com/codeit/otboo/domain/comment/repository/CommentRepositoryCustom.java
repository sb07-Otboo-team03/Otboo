package com.codeit.otboo.domain.comment.repository;

import com.codeit.otboo.domain.comment.entity.Comment;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface CommentRepositoryCustom {

    Slice<Comment> findAllByCursor(UUID feedId, String cursor, UUID idAfter, int limit);

    long countTotalElements(UUID feedId);
}
