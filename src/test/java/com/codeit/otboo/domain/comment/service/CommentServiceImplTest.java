package com.codeit.otboo.domain.comment.service;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.comment.dto.CommentCreateRequest;
import com.codeit.otboo.domain.comment.dto.CommentMapper;
import com.codeit.otboo.domain.comment.dto.CommentResponse;
import com.codeit.otboo.domain.comment.entity.Comment;
import com.codeit.otboo.domain.comment.repository.CommentRepository;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.exception.FeedNotFoundException;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.sse.event.SseEvent;
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
import org.mockito.ArgumentCaptor;
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

    @Mock private CommentRepository commentRepository;
    @Mock private FeedRepository feedRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private NotificationRepository notificationRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    // ❌ static이라 제거
    // @Mock private NotificationMapper notificationMapper;

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
            LocalDateTime now = LocalDateTime.now();

            User author = new User("author@a.a", "1234");
            ReflectionTestUtils.setField(author, "id", UUID.randomUUID());

            Feed feed = Feed.builder()
                .author(author)
                .build();

            User user = new User("user@a.a", "1234");
            ReflectionTestUtils.setField(user, "id", userId);
            new Profile(user, "user");

            CommentCreateRequest request =
                new CommentCreateRequest("Test Comment");

            CommentResponse response =
                CommentResponse.builder()
                    .content("Test Comment")
                    .build();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(commentMapper.toDto(any(Comment.class))).willReturn(response);

            // 🔥 핵심: Notification 저장 시 값 세팅
            given(notificationRepository.save(any(Notification.class)))
                .willAnswer(invocation -> {
                    Notification n = invocation.getArgument(0);
                    ReflectionTestUtils.setField(n, "id", UUID.randomUUID());
                    ReflectionTestUtils.setField(n, "createdAt", now);
                    return n;
                });

            // when
            CommentResponse result =
                commentService.createComment(feedId, userId, request);

            // then
            assertThat(result.content()).isEqualTo("Test Comment");

            verify(commentRepository).save(any(Comment.class));
            verify(notificationRepository).save(any(Notification.class));

            // 🔥 SSE 이벤트 검증
            ArgumentCaptor<SseEvent> captor =
                ArgumentCaptor.forClass(SseEvent.class);

            verify(eventPublisher).publishEvent(captor.capture());

            SseEvent event = captor.getValue();
            List<NotificationDto> dtos = event.notificationDtoList();

            assertThat(dtos).hasSize(1);

            NotificationDto dto = dtos.get(0);
            assertThat(dto.title()).contains("댓글을 달았어요");
            assertThat(dto.content()).isEqualTo("Test Comment");
            assertThat(dto.receiverId()).isEqualTo(author.getId());

            // 댓글 수 증가 검증
            assertThat(feed.getCommentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("댓글 생성 시, 존재하지 않는 피드Id라면 예외를 반환한다.")
        void createComment_Fail_NotFoundFeed() {
            UUID feedId = UUID.randomUUID();

            given(feedRepository.findById(feedId)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                commentService.createComment(feedId, null, null))
                .isInstanceOf(FeedNotFoundException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FEED_NOT_FOUND);
        }

        @Test
        @DisplayName("댓글 생성 시 존재하지 않는 유저Id라면 예외를 반환한다.")
        void createComment_Fail_NotFoundUser() {
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Feed feed = Feed.builder().build();

            given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                commentService.createComment(feedId, userId, null))
                .isInstanceOf(UserNotFoundException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    // ------------------------
    // 댓글 조회 (변경 없음)
    // ------------------------

    @Nested
    @DisplayName("댓글 조회")
    class CommentGet {

        @ParameterizedTest
        @CsvSource({
            "10, true",
            "5, false"
        })
        void getComment_Success(long total, boolean hasNext) {

            UUID feedId = UUID.randomUUID();
            List<Comment> comments = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                comments.add(mockComment(i));
            }

            Slice<Comment> slice =
                new SliceImpl<>(comments, PageRequest.of(0, 5), hasNext);

            given(feedRepository.existsById(feedId)).willReturn(true);
            given(commentRepository.findAllByCursor(any(), any(), any(), anyInt()))
                .willReturn(slice);
            given(commentRepository.countTotalElements(feedId))
                .willReturn(total);
            given(commentMapper.toDto(any(Comment.class))).willReturn(null);

            CursorResponse<CommentResponse> result =
                commentService.getAllComments(feedId, null, null, 5);

            assertThat(result.data()).hasSize(5);
        }
    }

    private Comment mockComment(int n) {
        Comment comment = Comment.builder().build();
        ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(comment, "createdAt",
            LocalDateTime.now().minusDays(n));
        return comment;
    }

    @Test
    @DisplayName("조회할 댓글이 없으면 바로 반환한다.")
    void getComment_IsEmpty() {
        UUID feedId = UUID.randomUUID();

        Slice<Comment> emptySlice =
            new SliceImpl<>(List.of(), PageRequest.of(0, 5), false);

        given(feedRepository.existsById(feedId)).willReturn(true);
        given(commentRepository.findAllByCursor(any(), any(), any(), anyInt()))
            .willReturn(emptySlice);

        CursorResponse<CommentResponse> result =
            commentService.getAllComments(feedId, null, null, 5);

        verify(commentMapper, never()).toDto(any());
        verify(commentRepository, never()).countTotalElements(any());

        assertThat(result.data()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 피드Id면 예외를 반환한다.")
    void getComment_Fail_NotFoundFeed() {
        UUID feedId = UUID.randomUUID();

        given(feedRepository.existsById(feedId)).willReturn(false);

        assertThatThrownBy(() ->
            commentService.getAllComments(feedId, null, null, 5))
            .isInstanceOf(FeedNotFoundException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FEED_NOT_FOUND);
    }
}