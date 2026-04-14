package com.codeit.otboo.domain.comment.service;

import com.codeit.otboo.domain.comment.dto.CommentCreateRequest;
import com.codeit.otboo.domain.comment.dto.CommentMapper;
import com.codeit.otboo.domain.comment.dto.CommentResponse;
import com.codeit.otboo.domain.comment.dto.CommentSearchRequest;
import com.codeit.otboo.domain.comment.entity.Comment;
import com.codeit.otboo.domain.comment.repository.CommentRepository;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.exception.FeedNotFoundException;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.sse.event.CommentCreatedEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponse createComment(UUID feedId, UUID userId, CommentCreateRequest request) {

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Comment comment = new Comment(request.content(), feed, user);
        commentRepository.save(comment);
        feed.increaseComment();

        String title = user.getProfile().getName() + "님이 댓글을 달았어요.";
        String content = comment.getContent();
        UUID receiverId = feed.getAuthor().getId();
        eventPublisher.publishEvent(new CommentCreatedEvent(title, content, receiverId));

        return commentMapper.toDto(comment);
    }

    @Override
    public CursorResponse<CommentResponse> getAllComments(CommentSearchRequest request) {

        UUID feedId = request.feedId();
        String cursor = request.cursor();
        UUID idAfter = request.idAfter();
        int limit = request.limit();

        if (!feedRepository.existsById(feedId)) throw new FeedNotFoundException(feedId);

        Slice<Comment> commentPage = commentRepository.findAllByCursor(feedId, cursor, idAfter, limit);

        List<Comment> content = commentPage.getContent();
        if (content.isEmpty())
            return new CursorResponse<>(List.of(), null, null,
                    false, 0L, "createdAt", SortDirection.DESCENDING);

        long totalCount = commentRepository.countTotalElements(feedId);

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (commentPage.hasNext()) {
            Comment lastComment = content.get(content.size() - 1);

            nextCursor = String.valueOf(lastComment.getCreatedAt());
            nextIdAfter = lastComment.getId();
        }

        List<CommentResponse> data = content.stream()
                .map(commentMapper::toDto)
                .toList();

        return new CursorResponse<>(data, nextCursor, nextIdAfter,
                commentPage.hasNext(), totalCount, "createdAt", SortDirection.DESCENDING);
    }
}
