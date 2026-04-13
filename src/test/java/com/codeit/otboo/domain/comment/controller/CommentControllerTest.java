package com.codeit.otboo.domain.comment.controller;

import com.codeit.otboo.domain.comment.dto.CommentCreateRequest;
import com.codeit.otboo.domain.comment.dto.CommentResponse;
import com.codeit.otboo.domain.comment.dto.CommentSearchRequest;
import com.codeit.otboo.domain.comment.service.CommentService;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = CommentController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private OtbooUserDetails userDetails;

    @BeforeEach
    void setup() {
        UserResponse userDto = UserResponse.builder().id(UUID.randomUUID()).role(Role.USER).build();
        userDetails = new OtbooUserDetails(userDto, "otboo123");
    }

    @Nested
    @DisplayName("댓글 생성")
    class CommentCreate {

        @Test
        @DisplayName("댓글 생성 성공")
        void createComment_Success() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID commentId = UUID.randomUUID();
            String content = "Test Comment";

            CommentCreateRequest request = new CommentCreateRequest(content);
            CommentResponse dto = CommentResponse.builder().id(commentId).feedId(feedId).content(content).build();
            given(commentService.createComment(eq(feedId), any(), eq(request))).willReturn(dto);

            // when & then
            mockMvc.perform(post("/api/feeds/{feedId}/comments", feedId)
                            .with(csrf())
                            .with(user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(commentId.toString()))
                    .andExpect(jsonPath("$.content").value(content));
        }
        
        @Test
        @DisplayName("내용이 없으면 댓글을 생성할 수 없다.")
        void createComment_Fail_EmptyContent() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();

            CommentCreateRequest request = new CommentCreateRequest(null);
            // when & then
            mockMvc.perform(post("/api/feeds/{feedId}/comments", feedId)
                            .with(user(userDetails))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class CommentSearch {

        @Test
        @DisplayName("댓글 조회")
        void commentSearch() throws Exception {
            // given
            UUID commentId = UUID.randomUUID();
            UUID feedId = UUID.randomUUID();
            CommentResponse dto = CommentResponse.builder()
                    .id(commentId)
                    .feedId(feedId)
                    .build();
            CursorResponse<CommentResponse> page
                    = new CursorResponse<>(List.of(dto), null, null, false,
                    1L, "createdAt", SortDirection.DESCENDING);

            given(commentService.getAllComments(any(CommentSearchRequest.class))).willReturn(page);

            // when & then
            mockMvc.perform(get("/api/feeds/{feedId}/comments", feedId)
                            .with(user(userDetails))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").value(commentId.toString()))
                    .andExpect(jsonPath("$.data[0].feedId").value(feedId.toString()));
        }
    }

}