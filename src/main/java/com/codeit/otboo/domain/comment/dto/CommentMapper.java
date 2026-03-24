package com.codeit.otboo.domain.comment.dto;

import com.codeit.otboo.domain.comment.entity.Comment;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserMapper userMapper;

    public CommentResponse toDto(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .createdAt(comment.getCreatedAt())
                .feedId(comment.getFeed().getId())
                .author(userMapper.toSummaryDto(
                        comment.getAuthor().getId(),
                        comment.getAuthor().getProfile().getName(),
                        comment.getAuthor().getProfile().getId()))
                .content(comment.getContent())
                .build();
    }
}