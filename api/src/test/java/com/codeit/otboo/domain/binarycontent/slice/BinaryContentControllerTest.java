package com.codeit.otboo.domain.binarycontent.slice;

import com.codeit.otboo.domain.binarycontent.controller.BinaryContentController;
import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentPresignedUrlRequest;
import com.codeit.otboo.domain.binarycontent.dto.response.BinaryContentPresignedUrlResponse;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BinaryContentController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )})
@AutoConfigureMockMvc(addFilters = false)
public class BinaryContentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BinaryContentService binaryContentService;

    @Nested
    @DisplayName("presigned url 발급")
    class PresignedUrl {
        @Test
        @DisplayName("성공: 유효한 요청이 들어올 경우 200으로 응답한다")
        void success_presigned_url() throws Exception {
            // given
            BinaryContentPresignedUrlRequest request = new BinaryContentPresignedUrlRequest(
                    "test.jpg", "image/jpeg", 10L);
            BinaryContentPresignedUrlResponse response = new BinaryContentPresignedUrlResponse(
                    UUID.randomUUID(), "https://presigned-url.com");

            given(binaryContentService.getPresignedUrl(request))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/binary-contents/images/presigned-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.binaryContentId").value(response.binaryContentId().toString()));
        }

        @Test
        @DisplayName("실패: fileName 이 공백일 경우 400 에러가 발생한다")
        void fail_presigned_url_fileName_is_blank() throws Exception {
            // given
            BinaryContentPresignedUrlRequest request = new BinaryContentPresignedUrlRequest(
                    "     ", "image/jpeg", 10L);

            // when & then
            mockMvc.perform(post("/api/binary-contents/images/presigned-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("실패: contentType 이 공백일 경우 400 에러가 발생한다")
        void fail_presigned_url_contentType_is_blank() throws Exception {
            // given
            BinaryContentPresignedUrlRequest request = new BinaryContentPresignedUrlRequest(
                    "test.jpg", "     ", 10L);

            // when & then
            mockMvc.perform(post("/api/binary-contents/images/presigned-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("실패: size가 음수일 경우 400 에러가 발생한다")
        void fail_presigned_url_size_is_minus() throws Exception {
            // given
            BinaryContentPresignedUrlRequest request = new BinaryContentPresignedUrlRequest(
                    "test.jpg", "image/jpeg", -10L);

            // when & then
            mockMvc.perform(post("/api/binary-contents/images/presigned-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("binaryContent 업로드 상태 변경")
    class UploadStatusChange{
        @Test
        @DisplayName("성공: 유효한 파라미터가 들어올 경우, 200으로 응답한다")
        void success_upload_status_change() throws Exception {
            // given
            UUID binaryContentId = UUID.randomUUID();

            // when & then
            mockMvc.perform(post("/api/binary-contents/{binaryContentId}/complete", binaryContentId))
                    .andExpect(status().isOk());

            then(binaryContentService).should().completeUpload(binaryContentId);
        }
    }
}
