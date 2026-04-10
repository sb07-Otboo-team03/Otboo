package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.binarycontent.mapper.BinaryContentMapper;
import com.codeit.otboo.domain.profile.ProfileFixture;
import com.codeit.otboo.domain.profile.dto.request.LocationRequest;
import com.codeit.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Gender;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.fixture.ProfileResponseFixture;
import com.codeit.otboo.domain.user.dto.request.UpdatePasswordRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserLockUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.UserCreateRequestFixture;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.fixture.UserResponseFixture;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )
        })
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BinaryContentMapper binaryContentMapper;


    @Nested
    @DisplayName("회원가입")
    class SignUp {

        @Test
        @DisplayName("회원가입 성공")
        void signUp_success() throws Exception {
            UserCreateRequest userCreateRequest = UserCreateRequestFixture.create();
            User user = UserFixture.create(UUID.randomUUID(), userCreateRequest.email(), userCreateRequest.password());
            UserResponse userResponse = UserResponseFixture.create(user);

            given(userService.createUser(userCreateRequest)).willReturn(userResponse);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateRequest)))
                    .andExpect(status().isCreated());

            then(userService).should().createUser(userCreateRequest);
        }

        @ParameterizedTest
        @CsvSource({
                "a, test@codeit.com, abcd12345", // name 길이 미충족 (최소 2)
                "ab, test, abcd15235", // 이메일 형식 아님
                ", , ", // 빈 값
                "ab, test@codeit.com, ab152", // 패스워드 길이 미충족
        })
        @DisplayName("로그인 실패 - BadRequest, 잘못된 파라미터 요청")
        void signUp_fail(String name, String email, String password) throws Exception {
            UserCreateRequest userCreateRequest = UserCreateRequestFixture.create(name, email, password);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateRequest)))
                    .andExpect(status().isBadRequest());

            then(userService).should(never()).createUser(userCreateRequest);
        }
    }

    @Nested
    @DisplayName("프로필 조회")
    class GetProfile {
        @Test
        @DisplayName("프로필 조회 성공")
        public void getProfile_success() throws Exception {
            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, "test@codeit.com", "password123!");
            Profile profile = ProfileFixture.create(user);
            user.setProfile(profile);
            profile.update(null, null, null, null, null, null);
            ProfileResponse profileResponse = ProfileResponseFixture.create(user, null);

            given(userService.getProfile(userId)).willReturn(profileResponse);

            mockMvc.perform(get("/api/users/{userId}/profiles", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId.toString()));
        }

        @Test
        @DisplayName("프로필 조회 실패 - 존재하지 않는 유저")
        public void getProfile_fail_notFound() throws Exception {
            UUID userId = UUID.randomUUID();

            given(userService.getProfile(userId)).willThrow(new UserNotFoundException(userId));

            mockMvc.perform(get("/api/users/{userId}/profiles", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class UpdatePassword {
        @Test
        @DisplayName("비밀번호 변경 성공")
        void update_password_success() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest("new_password");

            // when
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePasswordRequest)))
                    .andExpect(status().isNoContent());

            // then
            then(userService).should()
                    .updateUserPassword(eq(userId), any(UpdatePasswordRequest.class));

        }

        @Test
        @DisplayName("비밀번호 변경 실패 - 존재하지 않는 유저")
        void update_password_fail_userNotFound() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest("new_password");

            willThrow(new UserNotFoundException(userId))
                    .given(userService)
                    .updateUserPassword(eq(userId), any(UpdatePasswordRequest.class));

            // when
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePasswordRequest)))
                    .andExpect(status().isNotFound());

            // then
            then(userService).should()
                    .updateUserPassword(any(UUID.class), any(UpdatePasswordRequest.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "test"
        })
        @DisplayName("비밀번호 변경 실패 - 존재하지 않는 유저")
        void update_password_fail_badRequest(String password) throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest(password);

            // when
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePasswordRequest)))
                    .andExpect(status().isBadRequest());

            // then
            then(userService).should(never())
                    .updateUserPassword(any(UUID.class), any(UpdatePasswordRequest.class));
        }
    }

    @Nested
    @DisplayName("유저 목록 조회")
    class UserSearch {
        @Test
        @DisplayName("유저 목록 조회 - 성공")
        void userSearchList_success() throws Exception {
            User user = UserFixture.create();
            UserResponse userResponse = UserResponseFixture.create(user);
            List<UserResponse> userResponseList = new ArrayList<>();
            userResponseList.add(userResponse);

            CursorResponse<UserResponse> userResponseCursor = new CursorResponse<>(userResponseList, null, null, false, 1L, "createdAt", SortDirection.DESCENDING);

            given(userService.getAllUsers(any())).willReturn(userResponseCursor);

            mockMvc.perform(get("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userResponseCursor)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].email").value(user.getEmail()))
                    .andExpect(jsonPath("$.nextCursor").isEmpty());
        }

        @Test
        @DisplayName("유저 목록 조회 - 실패 (limit 음수)")
        void userSearchList_fail_badRequest() throws Exception {
            mockMvc.perform(get("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("limit", "-1"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("유저 프로필 업데이트")
    class ProfileUpdate {
        @Test
        @DisplayName("유저 프로필 업데이트 - 성공")
        void userUpdate_success() throws Exception {
            // given
            User user = UserFixture.create();
            UUID userId = UUID.randomUUID();

            ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest(
                    "새 이름",
                    Gender.MALE,
                    LocalDate.of(2000, 1, 1),
                    new LocationRequest(1.2, 1.3, 1, 2, List.of("서울시", "강남구", "역삼동", "")),
                    3
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(profileUpdateRequest)
            );

            ProfileResponse profileResponse = ProfileResponseFixture.create(user, null, profileUpdateRequest);

            given(userService.updateProfile(userId, profileUpdateRequest, null))
                    .willReturn(profileResponse);

            // when
            mockMvc.perform(multipart("/api/users/{userId}/profiles", userId)
                            .file(requestPart)
                            .with(servletRequest -> {
                                servletRequest.setMethod("PATCH");
                                return servletRequest;
                            }))
                    .andExpect(status().isOk());

            // then
            then(userService).should().updateProfile(userId, profileUpdateRequest, null);
        }

        @ParameterizedTest
        @CsvSource({
                "' ', 3",
                "이, 3",
                "이름, -1",
                "이름, 6"
        })
        @DisplayName("유저 프로필 업데이트 - 실패 (Bad Request) - 이름 길이를 충족하지 못하거나, ")
        void userUpdate_fail(String name, String temperatureSensitivity) throws Exception {
            // given
            UUID userId = UUID.randomUUID();

            ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest(
                    name,
                    Gender.MALE,
                    LocalDate.of(2000, 1, 1),
                    new LocationRequest(1.2, 1.3, 1, 2, List.of("서울시", "강남구", "역삼동", "")),
                    Integer.parseInt(temperatureSensitivity)
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(profileUpdateRequest)
            );


            // when
            mockMvc.perform(multipart("/api/users/{userId}/profiles", userId)
                            .file(requestPart)
                            .with(servletRequest -> {
                                servletRequest.setMethod("PATCH");
                                return servletRequest;
                            }))
                    .andExpect(status().isBadRequest());

            // then
            then(userService).should(never()).updateProfile(userId, profileUpdateRequest, null);
        }

        @Test
        @DisplayName("업데이트 실패 - 존재하지 않는 유저")
        void userUpdate_fail_notFound() throws Exception {
            // given
            UUID notFoundUserId = UUID.randomUUID();

            ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest(
                    "새 이름",
                    Gender.MALE,
                    LocalDate.of(2000, 1, 1),
                    new LocationRequest(1.2, 1.3, 1, 2, List.of("서울시", "강남구", "역삼동", "")),
                    3
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(profileUpdateRequest)
            );

            // when
            willThrow(new UserNotFoundException(notFoundUserId))
                    .given(userService)
                    .updateProfile(notFoundUserId, profileUpdateRequest, null);

            mockMvc.perform(multipart("/api/users/{userId}/profiles", notFoundUserId)
                            .file(requestPart)
                            .with(servletRequest -> {
                                servletRequest.setMethod("PATCH");
                                return servletRequest;
                            }))
                    .andExpect(status().isNotFound());

            // then
            then(userService).should().updateProfile(notFoundUserId, profileUpdateRequest, null);
        }
    }

    @Nested
    @DisplayName("유저 권한 변경")
    class UserRoleUpdate {
        @Test
        @DisplayName("유저 권한 변경 성공 200 응답")
        void updateRole_success() throws Exception {
            // given
            User user = UserFixture.create();
            UUID userId = user.getId();
            UserResponse userResponse = UserResponseFixture.create(user, Role.ADMIN);
            UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest(
                    Role.ADMIN
            );
            given(userService.updateUserRole(userId, userRoleUpdateRequest)).willReturn(userResponse);

            // when
            mockMvc.perform(patch("/api/users/{userId}/role", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRoleUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            // then
            then(userService).should().updateUserRole(userId, userRoleUpdateRequest);
        }

        @Test
        @DisplayName("유저 권한 변경 실패 - 존재하지 않는 유저")
        void updateRole_fail_userNotFound() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest(
                    Role.ADMIN
            );
            willThrow(new UserNotFoundException())
                    .given(userService)
                    .updateUserRole(userId, userRoleUpdateRequest);

            // when
            mockMvc.perform(patch("/api/users/{userId}/role", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRoleUpdateRequest)))
                    .andExpect(status().isNotFound());

            // then
            then(userService).should().updateUserRole(userId, userRoleUpdateRequest);
        }

        @Test
        @DisplayName("유저 권한 변경 실패 - BadRequest")
        void updateRole_fail_badRquest() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest(
                    null
            );
            // when
            mockMvc.perform(patch("/api/users/{userId}/role", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRoleUpdateRequest)))
                    .andExpect(status().isBadRequest());

            // then
            then(userService).should(never()).updateUserRole(userId, userRoleUpdateRequest);
        }
    }

    @Nested
    @DisplayName("유저 활성화 변경")
    class UserLockUpdate {
        @Test
        @DisplayName("유저 활성화 변경 성공 200 응답")
        void updateLock_success() throws Exception {
            // given
            User user = UserFixture.create(); // 기본 false
            UUID userId = user.getId();
            UserResponse userResponse = UserResponseFixture.create(user, true);
            UserLockUpdateRequest userLockUpdateRequest = new UserLockUpdateRequest(
                   true
            );

            given(userService.updateUserLockStatus(userId, userLockUpdateRequest)).willReturn(userResponse);

            // when
            mockMvc.perform(patch("/api/users/{userId}/lock", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userLockUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.locked").value("true"));

            // then
            then(userService).should().updateUserLockStatus(userId, userLockUpdateRequest);
        }

        @Test
        @DisplayName("유저 활성화 변경 실패 - 존재하지 않는 유저")
        void updateLock_fail_userNotFound() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UserLockUpdateRequest userLockUpdateRequest = new UserLockUpdateRequest(
                    true
            );
            willThrow(new UserNotFoundException())
                    .given(userService)
                    .updateUserLockStatus(userId, userLockUpdateRequest);

            // when
            mockMvc.perform(patch("/api/users/{userId}/lock", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userLockUpdateRequest)))
                    .andExpect(status().isNotFound());

            // then
            then(userService).should().updateUserLockStatus(userId, userLockUpdateRequest);
        }

        @Test
        @DisplayName("유저 권한 변경 실패 - BadRequest")
        void updateLock_fail_badRequest() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UserLockUpdateRequest userLockUpdateRequest = new UserLockUpdateRequest(
                    null
            );
            // when
            mockMvc.perform(patch("/api/users/{userId}/lock", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userLockUpdateRequest)))
                    .andExpect(status().isBadRequest());

            // then
            then(userService).should(never()).updateUserLockStatus(userId, userLockUpdateRequest);
        }
    }



}