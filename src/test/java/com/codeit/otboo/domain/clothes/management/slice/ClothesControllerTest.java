package com.codeit.otboo.domain.clothes.management.slice;

import com.codeit.otboo.domain.binarycontent.mapper.BinaryContentMapper;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.domain.clothes.management.controller.ClothesController;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCursorPageRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.exception.ClothesNotFoundException;
import com.codeit.otboo.domain.clothes.management.fixture.ClothesFixture;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.service.ClothesService;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.UserFixture;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            // when & then
            mockMvc.perform(
                        multipart("/api/clothes")
                                .file(requestPart)
                                .with(csrf())
                                .with(user(userDetails))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("실패: name이 blank로 들어올 경우 400 에러가 발생한다")
        void createClothes_Fail_BlankName() throws Exception {
            // given
            ClothesCreateRequest request = new ClothesCreateRequest(
                    user.getId(), " ", ClothesType.ETC, List.of());
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            // when & then
            mockMvc.perform(
                        multipart("/api/clothes")
                                .file(requestPart)
                                .with(csrf())
                                .with(user(userDetails))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("실패: type이 null로 들어올 경우 400 에러가 발생한다")
        void createClothes_Fail_NullType() throws Exception {
            ClothesCreateRequest request = new ClothesCreateRequest(
                    user.getId(), "새 옷", null, List.of()
            );
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            mockMvc.perform(
                            multipart("/api/clothes")
                                    .file(requestPart)
                                    .with(csrf())
                                    .with(user(userDetails))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("실패: ownerId가 존재하지 않는 유저의 ID일 경우 404 에러가 발생한다")
        void createClothes_Fail_Owner_NotFound() throws Exception {
            // given
            UUID ownerId = UUID.randomUUID();
            ClothesCreateRequest request = new ClothesCreateRequest(
                    ownerId, "새 옷", ClothesType.ETC, List.of());
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );
            willThrow(new UserNotFoundException(ownerId))
                    .given(clothesService)
                    .createClothes(null, request);

            // when & then
            mockMvc.perform(
                            multipart("/api/clothes")
                                    .file(requestPart)
                                    .with(csrf())
                                    .with(user(userDetails))
                    )
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("옷 삭제")
    class ClothesDelete {
        @Test
        @DisplayName("성공: 유효한 UUID 가 들어올경우 204로 응답한다")
        void deleteClothes_Success() throws Exception {
            // given
            UUID clothesId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/clothes/{clothesId}", clothesId)
                            .with(csrf())
                            .with(user(userDetails)))
                    .andExpect(status().isNoContent());
            then(clothesService).should().deleteClothes(eq(clothesId));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 UUID 가 들어올 경우 404 에러가 발생한다")
        void deleteClothes_Fail_NotFound() throws Exception {
            // given
            UUID clothesId = UUID.randomUUID();
            willThrow(new ClothesNotFoundException(clothesId))
                    .given(clothesService)
                    .deleteClothes(clothesId);

            // when & then
            mockMvc.perform(delete("/api/clothes/{clothesId}", clothesId)
                            .with(csrf())
                            .with(user(userDetails)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("옷 수정")
    class ClothesUpdate {
        @Test
        @DisplayName("성공: 유효한 요청이 들어올 경우 200으로 응답한다")
        void updateClothes_Success() throws Exception {
            // given
            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    "새 이름", ClothesType.ETC, List.of());
            Clothes clothes = ClothesFixture.create(
                    "옷", ClothesType.BAG, user, null, List.of());
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(),
                    clothes.getOwner().getId(),
                    request.name(),
                    null,
                    request.type(),
                    List.of()
            );
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );
            given(clothesService.updateClothes(clothes.getId(), null, request))
                    .willReturn(response);

            // when & then
            mockMvc.perform(
                            multipart("/api/clothes/{clothesId}", clothes.getId())
                                    .file(requestPart)
                                    .with(servletRequest -> {
                                        servletRequest.setMethod("PATCH");
                                        return servletRequest;
                                    })
                                    .with(csrf())
                                    .with(user(userDetails))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()));
        }

        @Test
        @DisplayName("실패: name이 blank로 들어올 경우 400 에러가 발생한다")
        void updateClothes_Fail_BlankName() throws Exception {
            // given
            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    "   ", ClothesType.ETC, List.of());
            Clothes clothes = ClothesFixture.create(
                    "옷", ClothesType.BAG, user, null, List.of());
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            // when & then
            mockMvc.perform(
                            multipart("/api/clothes/{clothesId}", clothes.getId())
                                    .file(requestPart)
                                    .with(servletRequest -> {
                                        servletRequest.setMethod("PATCH");
                                        return servletRequest;
                                    })
                                    .with(csrf())
                                    .with(user(userDetails))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("실패: type이 null로 들어올 경우 400 에러가 발생한다")
        void updateClothes_Fail_NullType() throws Exception {
            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    "   ", ClothesType.ETC, List.of());
            Clothes clothes = ClothesFixture.create(
                    "옷", ClothesType.BAG, user, null, List.of());
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            mockMvc.perform(
                            multipart("/api/clothes/{clothesId}", clothes.getId())
                                    .file(requestPart)
                                    .with(servletRequest -> {
                                        servletRequest.setMethod("PATCH");
                                        return servletRequest;
                                    })
                                    .with(csrf())
                                    .with(user(userDetails))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }
    }
    
    @Nested
    @DisplayName("옷 목록 조회")
    class getListClothes{
        @Test
        @DisplayName("성공: 유효한 요청이 들어올 경우 200으로 응답한다.")
        void getListClothes_Success() throws Exception {
            // given
            UUID ownerId = UUID.randomUUID();
            Clothes clothes = ClothesFixture.create();
            ClothesCursorPageRequest request = new ClothesCursorPageRequest(
                    null, null, null, null, ownerId);
            List<ClothesResponse> clothesList = List.of(
                    new ClothesResponse(clothes.getId(),
                            ownerId,
                            clothes.getName(),
                            null,
                            clothes.getType(),
                            List.of()
                    )
            );
            CursorResponse<ClothesResponse> response = new CursorResponse<>(
                    clothesList,
                    null,
                    null,
                    false,
                    1L,
                    "createdAt",
                    SortDirection.DESCENDING
            );
            given(clothesService.getClothesListByOwnerId(request))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/clothes")
                            .with(user(userDetails))
                            .param("ownerId", ownerId.toString())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").value(clothes.getId().toString()));
        }

        @Test
        @DisplayName("실패: limit가 1보다 작은 수가 들어오면 400 에러가 발생한다.")
        void getClothes_Fail_MinLimit() throws Exception {
            // given
            UUID ownerId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get("/api/clothes")
                            .with(user(userDetails))
                            .param("ownerId", ownerId.toString())
                            .param("limit", "-1")
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("실패: ownerId가 null 이면 에러가 발생한다.")
        void getClothes_Fail_Owner_Null() throws Exception {
            // when & then
            mockMvc.perform(get("/api/clothes")
                            .with(user(userDetails))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exceptionName").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("링크로 옷 등록")
    class ClothesLink {
        @Test
        @DisplayName("성공: 유효한 요청이 들어올 경우 200으로 응답한다")
        void getExtractions_Success() throws Exception {
            // given
            String url = "https://example.com";

            // when & then
            mockMvc.perform(get("/api/clothes/extractions")
                        .param("url", url)
                )
                .andExpect(status().isOk());
            then(clothesService).should().getClothesInfoByUrl(eq(url));
        }

        @Test
        @DisplayName("실패: url이 공백 문자로 들어올 경우 400으로 응답한다")
        void getExtractions_Fail_urlBlank() throws Exception {
            // when & then
            mockMvc.perform(get("/api/clothes/extractions")
                        .param("url", " ")
                )
                .andExpect(status().isBadRequest());
            then(clothesService).should(never()).getClothesInfoByUrl(anyString());
        }
    }
}