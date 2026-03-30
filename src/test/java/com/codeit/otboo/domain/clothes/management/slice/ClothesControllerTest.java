package com.codeit.otboo.domain.clothes.management.slice;

import com.codeit.otboo.domain.binarycontent.mapper.BinaryContentMapper;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.domain.clothes.management.controller.ClothesController;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.fixture.ClothesFixture;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.service.ClothesService;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClothesController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )})
@AutoConfigureMockMvc(addFilters = false)
public class ClothesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClothesService clothesService;

    @MockitoBean
    private BinaryContentMapper binaryContentMapper;

    @MockitoBean
    private ClothesMapper clothesMapper;

    @MockitoBean
    private BinaryContentService binaryContentService;

    private User user;
    private OtbooUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = UserFixture.create();
        UserResponse userDto = UserResponse.builder().id(user.getId()).role(Role.USER).build();
        userDetails = new OtbooUserDetails(userDto, "otboo123");
    }

    @Nested
    @DisplayName("옷 생성")
    class ClothesCreate {
        @Test
        @DisplayName("성공: 유효한 요청이 들어올 경우 201로 응답한다")
        void createClothes_Success() throws Exception {
            // given
            ClothesCreateRequest request = new ClothesCreateRequest(
                    user.getId(), "새 옷", ClothesType.ETC, List.of());
            Clothes clothes = ClothesFixture.create(request, null);
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(),
                    clothes.getOwner().getId(),
                    clothes.getName(),
                    null,
                    ClothesType.ETC,
                    List.of()
            );
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );
            given(clothesService.createClothes(null, request))
                    .willReturn(response);

            // when & then
            mockMvc.perform(
                    multipart("/api/clothes")
                            .file(requestPart)
                            .with(csrf())
                            .with(user(userDetails))
            )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION,
                            URI.create("/api/clothes/" + response.id()).toString()))
                    .andExpect(jsonPath("$.id").value(response.id().toString()));
        }

        @Test
        @DisplayName("실패: ownerId가 null 로 들어올 경우 400 에러가 발생한다")
        void createClothes_Fail_NullOwnerId() throws Exception {
            // given
            ClothesCreateRequest request = new ClothesCreateRequest(
                    null, "새 옷", ClothesType.ETC, List.of());

            // when & then
            mockMvc.perform(post("/api/clothes")
                            .with(csrf())
                            .with(user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }
    }
}