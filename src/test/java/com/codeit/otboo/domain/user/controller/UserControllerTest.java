package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.profile.ProfileFixture;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.fixture.ProfileResponseFixture;
import com.codeit.otboo.domain.user.dto.request.UpdatePasswordRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.UserCreateRequestFixture;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.fixture.UserResponseFixture;
import com.codeit.otboo.domain.user.service.AuthService;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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

            // when
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePasswordRequest)))
                    .andExpect(status().isUnauthorized());

            // then
            then(userService).should(never())
                    .updateUserPassword(any(UUID.class), any(UpdatePasswordRequest.class));
        }

    }

}