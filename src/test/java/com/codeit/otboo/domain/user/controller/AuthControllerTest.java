package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.fixture.UserResponseFixture;
import com.codeit.otboo.domain.user.service.AuthService;
import com.codeit.otboo.global.security.jwt.JwtProperties;
import com.codeit.otboo.global.security.jwt.JwtProvider;
import com.codeit.otboo.global.security.jwt.RefreshCookieFactory;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
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

            given(authService.signIn(username, password))
                    .willReturn(jwtInformation);
            given(jwtProperties.refreshTokenExpiration())
                    .willReturn(999L);
            given(refreshCookieFactory.create(anyString(), anyLong()))
                    .willReturn(cookie);

            // when & then
            mockMvc.perform(multipart("/api/auth/sign-in")
                            .file(new MockMultipartFile(
                                    "username",
                                    "",
                                    "text/plain",
                                    username.getBytes()
                            ))
                            .file(new MockMultipartFile(
                                    "password",
                                    "",
                                    "text/plain",
                                    password.getBytes()
                            ))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.userDto.email").value("test@codeit.com"))
                    .andExpect(cookie().exists(JwtProvider.REFRESH_TOKEN_COOKIE_NAME));

            then(authService).should().signIn(username, password);
            then(refreshCookieFactory).should().create(anyString(), anyLong());
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 인증 정보")
        void login_fail() throws Exception {
            UUID userId = UUID.randomUUID();
            password = "incorrect-password";
            User user = UserFixture.create(userId, username, password);
            given(authService.signIn(username, password))
                    .willThrow(new BadCredentialsException("인증 실패"));

            // when
            mockMvc.perform(multipart("/api/auth/sign-in")
                            .file(new MockMultipartFile(
                                    "username",
                                    "",
                                    "text/plain",
                                    username.getBytes()
                            ))
                            .file(new MockMultipartFile(
                                    "password",
                                    "",
                                    "text/plain",
                                    password.getBytes()
                            ))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(cookie().doesNotExist(JwtProvider.REFRESH_TOKEN_COOKIE_NAME));

            then(authService).should().signIn(username, password);
            then(refreshCookieFactory).should(never()).create(anyString(), anyLong());
        }
    }
}