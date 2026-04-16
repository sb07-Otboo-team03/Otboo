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
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private FeedRepository feedRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    CommentServiceImpl commentService;

    @Nested
    @DisplayName("댓글 생성")
    class CommentCreate {

        @Test
        @DisplayName("댓글을 생성할 수 있다.")
        void createComment_Success() {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            User author = new User("author@a.a", "otboo123");
            ReflectionTestUtils.setField(author, "id", UUID.randomUUID());
            Feed feed = Feed.builder().author(author).build();

            User user = new User("otboo@a.a", "otboo123");
            ReflectionTestUtils.setField(user, "id", userId);
            new Profile(user, "user");

            CommentCreateRequest request = new CommentCreateRequest("Test Comment");
            CommentResponse response = CommentResponse.builder().content("Test Comment").build();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(commentMapper.toDto(any(Comment.class))).willReturn(response);

            // when
            CommentResponse result = commentService.createComment(feedId, userId, request);

            // then
            assertThat(result.content()).isEqualTo(response.content());
        }

        @Test
        @DisplayName("댓글 생성 시, 존재하지 않는 피드Id라면 예외를 반환한다.")
        void createComment_Fail_NotFoundFeed() {
            // given
            UUID feedId = UUID.randomUUID();

            given(feedRepository.findById(feedId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(feedId, null, null))
                    .isInstanceOf(FeedNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FEED_NOT_FOUND);
        }

        @Test
        @DisplayName("댓글 생성 시 존재하지 유저Id라면 예외를 반환한다.")
        void createComment_Fail_NotFoundUser() {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Feed feed = Feed.builder().build();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(feedId, userId, null))
                    .isInstanceOf(UserNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("댓글 조회")
    class CommentGet {

        @ParameterizedTest
        @CsvSource({
                "10, true",
                "5, false"
        })
        @DisplayName("""
                댓글을 조회할 수 있다.
                다음 페이지가 없으면 hasNext는 false이다.
                sortBy: createdAt
                sortDirection: DESCENDING
                limit: 5
                total: 10, 5
                hasNext: true, false
                """)
        void getComment_Success(long total, boolean hasNext) {
            // given
            UUID feedId = UUID.randomUUID();
            List<Comment> comments = new ArrayList<>();
            CommentSearchRequest request = new CommentSearchRequest(feedId, null, null, 5);

            for (int i = 0; i < 5; i++) {
                comments.add(mockComment(i));
            }

            Slice<Comment> slice = new SliceImpl<>(comments, PageRequest.of(0, 5), hasNext);

            given(feedRepository.findCommentCountByFeedId(feedId)).willReturn(Optional.of((int)total));
            given(commentRepository.findAllByCursor(any(), any(), any(), anyInt())).willReturn(slice);
            given(commentMapper.toDto(any(Comment.class))).willReturn(null);

            // when
            CursorResponse<CommentResponse> result = commentService.getAllComments(request);

            // then
            assertThat(result.data()).hasSize(5);

            Comment lastComment = comments.get(4);
            if (total > 5){
                assertThat(result.hasNext()).isTrue();
                assertThat(result.nextCursor()).isEqualTo(lastComment.getCreatedAt().toString());
                assertThat(result.nextIdAfter()).isEqualTo(lastComment.getId());

            }
            else {
                assertThat(result.hasNext()).isFalse();
                assertThat(result.nextCursor()).isNull();
                assertThat(result.nextIdAfter()).isNull();
            }
        }
    }

    private Comment mockComment(int n) {
        Comment comment = Comment.builder().build();
        ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now().minusDays(n));
        return comment;
    }

    @Test
    @DisplayName("조회할 댓글이 없으면 바로 반환한다.")
    void getComment_IsEmpty() {
        UUID feedId = UUID.randomUUID();
        List<Comment> comments = List.of();
        CommentSearchRequest request = new CommentSearchRequest(feedId, null, null, 5);

        Slice<Comment> emptySlice = new SliceImpl<>(comments, PageRequest.of(0, 5), false);

        given(feedRepository.findCommentCountByFeedId(feedId)).willReturn(Optional.of(0));

        // when
        CursorResponse<CommentResponse> result = commentService.getAllComments(request);

        // then
        verify(commentMapper, never()).toDto(any(Comment.class));
        verify(commentRepository, never()).findAllByCursor(any(), any(), any(), anyInt());

        assertThat(result.data()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 피드Id면 예외를 반환한다.")
    void getComment_Fail_NotFoundFeed() {
        // given
        UUID feedId = UUID.randomUUID();
        CommentSearchRequest request = new CommentSearchRequest(feedId, null, null, 5);

        given(feedRepository.findCommentCountByFeedId(feedId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getAllComments(request))
                .isInstanceOf(FeedNotFoundException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FEED_NOT_FOUND);
    }
}