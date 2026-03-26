package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.AuthStatePersistentException;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtProperties;
import com.codeit.otboo.global.security.jwt.JwtProvider;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;
import com.codeit.otboo.global.security.jwt.exception.JwtExpiredTokenException;
import com.codeit.otboo.global.security.jwt.registry.RedisRegistry;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RedisRegistry redisRegistry;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @DisplayName("로그인 테스트")
    @Nested
    class SignInTest {
        private String accessToken;
        private String refreshToken;
        private long refreshTokenExpiration;

        @BeforeEach
        void setUp() {
            accessToken = "access-token";
            refreshToken = "refresh-token";
            refreshTokenExpiration = 1209600L;
        }

        @Test
        @DisplayName("정상 로그인")
        void signIn_success() {
            // given
            String email = "test@test.com";
            String password = "1234";
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

            UUID userId = UUID.randomUUID();
            UserResponse userResponse = UserResponse.builder()
                    .id(userId)
                    .createdAt(createdAt)
                    .email(email)
                    .name("test")
                    .role(Role.USER)
                    .locked(false)
                    .build();

            Authentication authentication = mock(Authentication.class);
            OtbooUserDetails userDetails = mock(OtbooUserDetails.class);

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(userDetails);
            given(userDetails.getUserResponse()).willReturn(userResponse);

            given(jwtProvider.generateRefreshToken(eq(userId), eq(email), any(String.class)))
                    .willReturn(refreshToken);
            given(jwtProperties.refreshTokenExpiration()).willReturn(refreshTokenExpiration);
            given(jwtProvider.generateAccessToken(eq(userId), eq(email), any(String.class)))
                    .willReturn(accessToken);

            ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);

            // when
            JwtInformation result = authService.signIn(email, password);

            // then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(accessToken);
            assertThat(result.refreshToken()).isEqualTo(refreshToken);
            assertThat(result.userResponse()).isEqualTo(userResponse);

            then(authenticationManager).should()
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            then(redisRegistry).should()
                    .save(eq(userId), sessionIdCaptor.capture(), eq(refreshToken), eq(refreshTokenExpiration));

            String savedSessionId = sessionIdCaptor.getValue();
            assertThat(savedSessionId).isNotBlank();

            then(jwtProvider).should()
                    .generateRefreshToken(eq(userId), eq(email), eq(savedSessionId));
            then(jwtProvider).should()
                    .generateAccessToken(eq(userId), eq(email), eq(savedSessionId));
        }

        @DisplayName("로그인 실패 - 아이디 또는 패스워드 실패")
        @ParameterizedTest
        @CsvSource({
                "test_fail@codeit.com, password123!", // id 실패
                "test@codeit.com, password_fail123!", // password 실패
        })
        void signIn_fail(String email, String password) {
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            // when & then
            assertThatThrownBy(() -> authService.signIn(email, password))
                    .isInstanceOf(BadCredentialsException.class);

            then(jwtProvider).shouldHaveNoInteractions();
            then(redisRegistry).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("로그인은 성공했지만, Redis 저장에 실패")
        void signIn_fail_authStatePersistence() {
            String email = "test@test.com";
            String password = "1234";
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

            UUID userId = UUID.randomUUID();
            UserResponse userResponse = UserResponse.builder()
                    .id(userId)
                    .createdAt(createdAt)
                    .email(email)
                    .name("test")
                    .role(Role.USER)
                    .locked(false)
                    .build();

            Authentication authentication = mock(Authentication.class);
            OtbooUserDetails userDetails = mock(OtbooUserDetails.class);

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(userDetails);
            given(userDetails.getUserResponse()).willReturn(userResponse);

            given(jwtProvider.generateRefreshToken(eq(userId), eq(email), any(String.class)))
                    .willReturn(refreshToken);
            given(jwtProperties.refreshTokenExpiration()).willReturn(refreshTokenExpiration);

            willThrow(new RuntimeException())
                    .given(redisRegistry)
                    .save(eq(userId), any(String.class), eq(refreshToken), eq(refreshTokenExpiration));

            // when & then
            assertThatThrownBy(() -> authService.signIn(email, password))
                    .isInstanceOf(AuthStatePersistentException.class);

            then(redisRegistry).should().delete(userId);
            then(jwtProvider).shouldHaveNoMoreInteractions();
        }

        // NOTE & TODO: 계정 비활성화 기능은 아직 만들지 않았지만,
        // 이미 존재하는 유저가, 비활성화된 경우 처리,
        @DisplayName("비활성화 계정은 로그인할 수 없다")
        @Test
        void signIn_fail_lockedUser() {
            // given
            String email = "test@test.com";
            String password = "1234";

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new LockedException("Account is locked"));

            // when & then
            assertThatThrownBy(() -> authService.signIn(email, password))
                    .isInstanceOf(LockedException.class);

            then(jwtProvider).shouldHaveNoInteractions();
            then(redisRegistry).shouldHaveNoInteractions();
        }
    }

    @DisplayName("리프레시 토큰 테스트")
    @Nested
    class RefreshTokenTest {
        private UUID userId;
        private String email;
        private String password;
        private String refreshToken;
        private String newRefreshToken;
        private String accessToken;
        private String sessionId;
        private long refreshTokenExpiration;
        private JWTClaimsSet claimsSet;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            email = "test@test.com";
            refreshToken = "refresh-token";
            newRefreshToken = "new-refresh-token";
            password = "1234";
            accessToken = "new-access-token";
            sessionId = "session-id";
            refreshTokenExpiration = 1209600L;

            claimsSet = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .build();
        }

        @Test
        @DisplayName("정상적으로 리프레쉬 토큰 재발급")
        void refreshToken_success() {
            // given
            User user = UserFixture.create(userId, email, password);
            UserResponse userResponse = UserResponse.builder()
                    .id(userId)
                    .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                    .email(email)
                    .name("test")
                    .role(Role.USER)
                    .locked(false)
                    .build();

            given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(claimsSet);
            given(redisRegistry.isValidRefreshToken(userId, refreshToken)).willReturn(true);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(jwtProvider.getSessionId(refreshToken)).willReturn(sessionId);
            given(jwtProvider.getEmail(refreshToken)).willReturn(email);
            given(jwtProvider.generateRefreshToken(userId, email, sessionId)).willReturn(newRefreshToken);
            given(jwtProperties.refreshTokenExpiration()).willReturn(refreshTokenExpiration);
            given(jwtProvider.generateAccessToken(userId, email, sessionId)).willReturn(accessToken);
            given(userMapper.toDto(user)).willReturn(userResponse);

            // when
            JwtInformation result = authService.refreshToken(refreshToken);

            // then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(accessToken);
            assertThat(result.refreshToken()).isEqualTo(newRefreshToken);
            assertThat(result.userResponse()).isEqualTo(userResponse);

            then(jwtProvider).should().validateRefreshToken(refreshToken);
            then(redisRegistry).should().isValidRefreshToken(userId, refreshToken);
            then(userRepository).should().findById(userId);
            then(redisRegistry).should()
                    .rotateRefreshToken(userId, refreshToken, newRefreshToken, refreshTokenExpiration);
            then(jwtProvider).should().generateAccessToken(userId, email, sessionId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 ID(Claims)를 가진 Refresh 토큰이면 예외 발생")
        void refreshToken_fail_notFoundUser() {
            // given
            String refreshToken = "invalid-refresh-token";
            given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(claimsSet);
            given(redisRegistry.isValidRefreshToken(userId, refreshToken)).willReturn(true);
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                    .isInstanceOf(UserNotFoundException.class);

            then(redisRegistry).should().isValidRefreshToken(userId, refreshToken);
            then(jwtProvider).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 (Redis에 없는) Refresh 토큰이면 예외 발생")
        void refreshToken_fail_notExistRefresh() {
            // given
            given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(claimsSet);
            given(redisRegistry.isValidRefreshToken(userId, refreshToken)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                    .isInstanceOf(JwtExpiredTokenException.class);

            then(userRepository).shouldHaveNoInteractions();
            then(jwtProvider).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Redis rotate 실패 시 저장 예외가 발생한다")
        void refreshToken_fail_authStatePersistence() {
            // given
            User user = mock(User.class);

            given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(claimsSet);
            given(redisRegistry.isValidRefreshToken(userId, refreshToken)).willReturn(true);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(jwtProvider.getSessionId(refreshToken)).willReturn(sessionId);
            given(jwtProvider.getEmail(refreshToken)).willReturn(email);
            given(jwtProvider.generateRefreshToken(userId, email, sessionId)).willReturn(newRefreshToken);
            given(jwtProperties.refreshTokenExpiration()).willReturn(refreshTokenExpiration);

            willThrow(new RuntimeException("redis rotate fail"))
                    .given(redisRegistry)
                    .rotateRefreshToken(userId, refreshToken, newRefreshToken, refreshTokenExpiration);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                    .isInstanceOf(AuthStatePersistentException.class);

            then(redisRegistry).should().delete(userId);
            then(jwtProvider).should(never()).generateAccessToken(any(), any(), any());
        }

    }
}