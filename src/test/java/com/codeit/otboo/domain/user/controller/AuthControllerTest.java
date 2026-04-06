package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.user.dto.request.PasswordResetRequest;
import com.codeit.otboo.domain.user.dto.request.SignInRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.TemporaryPasswordMailSendFailedException;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.fixture.UserResponseFixture;
import com.codeit.otboo.domain.user.service.AuthService;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.security.jwt.JwtProperties;
import com.codeit.otboo.global.security.jwt.JwtProvider;
import com.codeit.otboo.global.security.jwt.RefreshCookieFactory;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;
import com.codeit.otboo.global.security.jwt.exception.JwtExpiredTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.web.servlet.function.RequestPredicates.param;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )
        })
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RefreshCookieFactory refreshCookieFactory;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("로그인")
    class SignInTest {
        String username;
        String password;

        @BeforeEach
        void setUp() {
            username = "test@codeit.com";
            password = "password123!";
        }

        @Test
        @DisplayName("로그인 성공")
        void login_success() throws Exception {
            UUID userId = UUID.randomUUID();
            SignInRequest signInRequest = new SignInRequest(username, password);
            User user = UserFixture.create(userId, username, password);
            UserResponse userResponse = UserResponseFixture.create(user);

            String accessToken = "access-token";
            String refreshToken = "refresh-token";

            JwtInformation jwtInformation = new JwtInformation(
                    userResponse,
                    accessToken,
                    refreshToken
            );

            Cookie cookie = new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken);

            given(authService.signIn(signInRequest))
                    .willReturn(jwtInformation);
            given(jwtProperties.refreshTokenExpiration())
                    .willReturn(999L);
            given(refreshCookieFactory.create(anyString(), anyLong()))
                    .willReturn(cookie);

            // when
            mockMvc.perform(multipart("/api/auth/sign-in")
                            .param("username", username)
                            .param("password", password)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.userDto.email").value("test@codeit.com"))
                    .andExpect(cookie().exists(JwtProvider.REFRESH_TOKEN_COOKIE_NAME))
                    .andExpect(cookie().value(
                            JwtProvider.REFRESH_TOKEN_COOKIE_NAME,
                            "refresh-token"
                    ));

            // then
            then(authService).should().signIn(signInRequest);
            then(refreshCookieFactory).should().create(anyString(), anyLong());
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 인증 정보")
        void login_fail() throws Exception {
            // given
            password = "incorrect-password";
            SignInRequest signInRequest = new SignInRequest(username, password);
            given(authService.signIn(new SignInRequest(username, password)))
                    .willThrow(new BadCredentialsException("인증 실패"));

            // when
            mockMvc.perform(multipart("/api/auth/sign-in")
                            .param("username", username)
                            .param("password", password)
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(cookie().doesNotExist(JwtProvider.REFRESH_TOKEN_COOKIE_NAME));

            // then
            then(authService).should().signIn(signInRequest);
            then(refreshCookieFactory).should(never()).create(anyString(), anyLong());
        }

        @ParameterizedTest
        @CsvSource({
                ", password123!",
                ", ",
                "test@codeit.com, ",
                ", ppaaosss2", // 이메일 아님
                "test@codeit.com, pass" // password 길이 미충족

        })
        @DisplayName("로그인 실패 - BadRequest, 잘못된 파라미터 요청")
        void login_fail_badRequest(String username, String password) throws Exception {
            mockMvc.perform(multipart("/api/auth/sign-in")
                            .param("username", username)
                            .param("password", password))
                    .andExpect(status().isBadRequest());
        }

        // TODO: Lock이 된 계정의 테스트는, 계정 비활성화 기능 구현 이후 진행하겠습니다.
    }

    @Nested
    @DisplayName("Refresh 토큰 재발급")
    class RefreshToken {
        String username;
        String password;

        @BeforeEach
        void setUp() {
            username = "test@codeit.com";
            password = "password123!";
        }

        @Test
        @DisplayName("정상적인 토큰 재발급")
        void refreshToken_success() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, username, password);
            UserResponse userResponse = UserResponseFixture.create(user);

            String accessToken = "access-token";
            String refreshToken = "refresh-token";
            String newRefreshToken = "new-refresh-token";
            Cookie cookie = new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, newRefreshToken);

            JwtInformation jwtInformation = new JwtInformation(
                    userResponse,
                    accessToken,
                    newRefreshToken
            );

            given(authService.refreshToken(refreshToken))
                    .willReturn(jwtInformation);
            given(jwtProperties.refreshTokenExpiration())
                    .willReturn(999L);
            given(refreshCookieFactory.create(anyString(), anyLong()))
                    .willReturn(cookie);

            // when
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken))
                    )
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists(JwtProvider.REFRESH_TOKEN_COOKIE_NAME))
                    .andExpect(cookie().value(
                            JwtProvider.REFRESH_TOKEN_COOKIE_NAME,
                            "new-refresh-token"));

            then(authService).should().refreshToken(anyString());
            then(refreshCookieFactory).should().create(anyString(), anyLong());
        }

        @Test
        @DisplayName("토큰 재발급 실패 - refresh token 쿠키 없이 ")
        void refreshToken_fail_notExistRefreshCookie() throws Exception {
            given(authService.refreshToken(""))
                    .willThrow(new JwtExpiredTokenException());

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, "")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(cookie().doesNotExist(JwtProvider.REFRESH_TOKEN_COOKIE_NAME));

            then(authService).should().refreshToken("");
            then(refreshCookieFactory).should(never()).create(anyString(), anyLong());
        }

        @Test
        @DisplayName("토큰 재발급 실패 - refresh token 쿠키가 잘못된 경우 ")
        void refreshToken_fail_invalidCookie() throws Exception {
            String refreshToken = "invalid-refresh-token";

            given(authService.refreshToken(refreshToken))
                    .willThrow(new JwtExpiredTokenException());

            // when
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(cookie().doesNotExist(JwtProvider.REFRESH_TOKEN_COOKIE_NAME));

            //  then
            then(authService).should().refreshToken(anyString());
            then(refreshCookieFactory).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("비밀번호 초기화")
    class PasswordReset {

        @Test
        @DisplayName("비밀번호 초기화 성공")
        void resetPassword_success() throws Exception{
            // given
            String email = "test@codiet.com";
            PasswordResetRequest passwordResetRequest = new PasswordResetRequest(email);
            willDoNothing().given(authService).issueTemporaryPassword(any(PasswordResetRequest.class));

            // when & then
            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordResetRequest)))
                    .andExpect(status().isNoContent());

            then(authService).should().issueTemporaryPassword(any(PasswordResetRequest.class));

        }

        @Test
        @DisplayName("비밀번호 초기화 실패 - 존재하지 않는 유저")
        void resetPassword_failed_notFoundEmail() throws Exception{
            // given
            String email = "notFoundUser@codeit.com";
            PasswordResetRequest passwordResetRequest = new PasswordResetRequest(email);
            willThrow(new UserNotFoundException("test@codeit.com"))
                    .given(authService).issueTemporaryPassword(any(PasswordResetRequest.class));

            // when & then
            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordResetRequest)))
                    .andExpect(status().isNotFound());

            then(authService).should().issueTemporaryPassword(any(PasswordResetRequest.class));
        }

        @Test
        @DisplayName("실패 - 메일 발송 실패")
        void password_reset_failed_mailSend() throws Exception {
            // given
            PasswordResetRequest request = new PasswordResetRequest("test@codeit.com");

            willThrow(new TemporaryPasswordMailSendFailedException())
                    .given(authService).issueTemporaryPassword(any(PasswordResetRequest.class));

            // when & then
            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());

            then(authService).should().issueTemporaryPassword(any(PasswordResetRequest.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "test"
        })
        @DisplayName("실패 - 잘못된 파라미터 요청")
        void password_reset_fail_badRequest() throws Exception {
            // given
            PasswordResetRequest request = new PasswordResetRequest("test"); // 이메일 형식 아님


            // when & then
            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(authService).should(never()).issueTemporaryPassword(any(PasswordResetRequest.class));
        }
    }
}