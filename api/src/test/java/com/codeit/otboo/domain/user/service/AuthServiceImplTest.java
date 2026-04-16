package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.dto.request.PasswordResetRequest;
import com.codeit.otboo.domain.user.dto.request.SignInRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.TemporaryPassword;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.AuthStatePersistentException;
import com.codeit.otboo.domain.user.exception.TemporaryPasswordMailSendFailedException;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.TemporaryPasswordFixture;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.fixture.UserResponseFixture;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.TemporaryPasswordRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TemporaryPasswordRepository temporaryPasswordRepository;
    @Mock
    private MailService mailService;
    @Mock
    private UserDetailsService userDetailsService;


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
        @DisplayName("정상 로그인 - 기존 비밀번호로 로그인.")
        void signIn_success() {
            // given
            String email = "test@test.com";
            String password = "1234";
            SignInRequest signInRequest = new SignInRequest(email, password);

            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, email, password);
            UserResponse userResponse = UserResponseFixture.create(user);

            Authentication authentication = mock(Authentication.class);
            OtbooUserDetails userDetails = mock(OtbooUserDetails.class);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.empty());
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
            JwtInformation result = authService.signIn(signInRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(accessToken);
            assertThat(result.refreshToken()).isEqualTo(refreshToken);
            assertThat(result.userResponse()).isEqualTo(userResponse);

            then(userRepository).should().findByEmail(email);
            then(temporaryPasswordRepository).should().findByUserId(userId);

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
        
        @Test
        @DisplayName("정상 로그인 - 임시비밀번호 사용")
        void singIn_success_withTemporaryPassword() {
            // given
            String email = "test@test.com";
            String rawPassword = "temp-1234";
            String encodedTempPassword = "encoded-temp-password";
            SignInRequest signInRequest = new SignInRequest(email, rawPassword);

            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, email, "origin-password");
            UserResponse userResponse = UserResponseFixture.create(user);

            TemporaryPassword temporaryPassword = TemporaryPasswordFixture.create(user,encodedTempPassword, LocalDateTime.now().plusMinutes(3));
            OtbooUserDetails userDetails = mock(OtbooUserDetails.class);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.of(temporaryPassword));
            given(passwordEncoder.matches(rawPassword, encodedTempPassword)).willReturn(true);
            given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);
            given(userDetails.isAccountNonLocked()).willReturn(true);
            given(userDetails.getUserResponse()).willReturn(userResponse);

            given(jwtProvider.generateRefreshToken(eq(userId), eq(email), any(String.class)))
                    .willReturn(refreshToken);
            given(jwtProperties.refreshTokenExpiration()).willReturn(refreshTokenExpiration);
            given(jwtProvider.generateAccessToken(eq(userId), eq(email), any(String.class)))
                    .willReturn(accessToken);

            ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);

            // when
            JwtInformation result = authService.signIn(signInRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(accessToken);
            assertThat(result.refreshToken()).isEqualTo(refreshToken);
            assertThat(result.userResponse()).isEqualTo(userResponse);

            then(userRepository).should().findByEmail(email);
            then(temporaryPasswordRepository).should().findByUserId(userId);
            then(passwordEncoder).should().matches(rawPassword, encodedTempPassword);
            then(userDetailsService).should().loadUserByUsername(email);
            then(authenticationManager).shouldHaveNoInteractions();

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
            // given
            SignInRequest signInRequest = new SignInRequest(email, password);
            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, email, "origin-password");
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.empty());
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            // when & then
            assertThatThrownBy(() -> authService.signIn(signInRequest))
                    .isInstanceOf(BadCredentialsException.class);

            then(userRepository).should().findByEmail(email);
            then(temporaryPasswordRepository).should().findByUserId(userId);
            then(jwtProvider).shouldHaveNoInteractions();
            then(redisRegistry).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("로그인은 성공했지만, Redis 저장에 실패")
        void signIn_fail_authStatePersistence() {
            String email = "test@test.com";
            String password = "1234";
            SignInRequest signInRequest = new SignInRequest(email, password);

            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, email, password);
            UserResponse userResponse = UserResponseFixture.create(user);

            Authentication authentication = mock(Authentication.class);
            OtbooUserDetails userDetails = mock(OtbooUserDetails.class);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.empty());
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
            assertThatThrownBy(() -> authService.signIn(signInRequest))
                    .isInstanceOf(AuthStatePersistentException.class);

            then(userRepository).should().findByEmail(email);
            then(temporaryPasswordRepository).should().findByUserId(userId);
            then(redisRegistry).should().delete(userId);
            then(jwtProvider).shouldHaveNoMoreInteractions();
        }

        // 이미 존재하는 유저가, 잠금된 경우 처리,
        @DisplayName("잠김 계정은 로그인할 수 없다")
        @Test
        void signIn_fail_lockedUser() {
            // given
            String email = "test@test.com";
            String password = "1234";
            SignInRequest signInRequest = new SignInRequest(email, password);
            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, email, "origin-password");

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.empty());
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new LockedException("Account is locked"));

            // when & then
            assertThatThrownBy(() -> authService.signIn(signInRequest))
                    .isInstanceOf(LockedException.class);


            then(jwtProvider).shouldHaveNoInteractions();
            then(redisRegistry).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("로그인 실패 - 임시 비밀번호 비밀번호 실패")
        void singIn_fail_temporaryPassword_invalid() {
            // given
            String email = "test@test.com";
            String rawPassword = "temp-1234";
            String encodedTempPassword = "encoded-temp-password";
            SignInRequest signInRequest = new SignInRequest(email, rawPassword);

            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, email, "origin-password");

            TemporaryPassword temporaryPassword = TemporaryPasswordFixture.create(user,encodedTempPassword, LocalDateTime.now().plusMinutes(3));

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.of(temporaryPassword));
            given(passwordEncoder.matches(rawPassword, encodedTempPassword)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.signIn(signInRequest))
                    .isInstanceOf(BadCredentialsException.class);

            then(authenticationManager).shouldHaveNoInteractions();
            then(jwtProvider).shouldHaveNoInteractions();
            then(redisRegistry).shouldHaveNoInteractions();

        }

        @Test
        @DisplayName("로그인 실패 - 임시 비밀번호 로그인 시 계정 잠김으로 인한 실패")
        void signIn_fail_lockedUser_withTemporaryPassword() {
            // given
            String email = "test@test.com";
            String rawPassword = "temp-1234";
            String encodedTempPassword = "encoded-temp-password";
            SignInRequest signInRequest = new SignInRequest(email, rawPassword);

            UUID userId = UUID.randomUUID();
            User user = UserFixture.create(userId, email, "origin-password");

            TemporaryPassword temporaryPassword = TemporaryPasswordFixture.create(user,encodedTempPassword, LocalDateTime.now().plusMinutes(3));
            OtbooUserDetails userDetails = mock(OtbooUserDetails.class);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.of(temporaryPassword));
            given(passwordEncoder.matches(rawPassword, encodedTempPassword)).willReturn(true);

            given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);
            given(userDetails.isAccountNonLocked()).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.signIn(signInRequest))
                    .isInstanceOf(LockedException.class);

            then(authenticationManager).shouldHaveNoInteractions();
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
            UserResponse userResponse = UserResponseFixture.create(user);

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
            then(userRepository).should().findById(userId);
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
            User user = UserFixture.create(userId, email, password);

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

    @Nested
    @DisplayName("로그 아웃")
    class SignOut {
        @Test
        @DisplayName("로그아웃 성공 - redis 정보 삭제")
        void logout_success() {
            // given
            UUID userId = UUID.randomUUID();

            // when
            authService.signOut(userId);

            // then
            then(redisRegistry).should().delete(userId);
            then(redisRegistry).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("비밀번호 초기화")
    class PasswordReset {
        private String email;
        private User user;
        private UUID userId;

        private String encodedPassword;
        private LocalDateTime expiresAt;
        private TemporaryPassword temporaryPassword;

        @BeforeEach
        void setUp() {
            email = "test@codeit.com";
            encodedPassword = "encodedTemporaryPassword";
            user = UserFixture.create(email, "testPassword");
            expiresAt = LocalDateTime.now().plusMinutes(3);
            temporaryPassword = TemporaryPasswordFixture.create(user, encodedPassword, expiresAt);
            userId = user.getId();
        }

        @Test
        @DisplayName("비밀번호 초기화 성공 - 기존 임시 비밀번호가 없는 경우 (save)")
        void password_reset_success_save() {
            // given
            PasswordResetRequest passwordResetRequest = new PasswordResetRequest(email);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);

            ArgumentCaptor<TemporaryPassword> temporaryPasswordCaptor =
                    ArgumentCaptor.forClass(TemporaryPassword.class);
            ArgumentCaptor<String> temporaryPasswordArgCaptor =
                    ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> expiresAtArgCaptor =
                    ArgumentCaptor.forClass(String.class);

            // when
            authService.issueTemporaryPassword(passwordResetRequest);

            // then
            then(userRepository).should().findByEmail(email);
            then(temporaryPasswordRepository).should().findByUserId(userId);
            then(passwordEncoder).should().encode(anyString());
            then(temporaryPasswordRepository).should().save(temporaryPasswordCaptor.capture());
            then(mailService).should().sendTemporaryPassword(
                    eq(email),
                    temporaryPasswordArgCaptor.capture(),
                    expiresAtArgCaptor.capture()
            );

            TemporaryPassword savedTemporaryPassword = temporaryPasswordCaptor.getValue();

            assertThat(savedTemporaryPassword.getUser()).isEqualTo(user);
            assertThat(savedTemporaryPassword.getPassword()).isEqualTo(encodedPassword);
            assertThat(savedTemporaryPassword.isExpired()).isFalse();

            assertThat(temporaryPasswordArgCaptor.getValue()).isNotBlank();
            assertThat(expiresAtArgCaptor.getValue()).isNotBlank();

        }

        @Test
        @DisplayName("비밀번호 초기화 성공 - 기존 임시 비밀번호가 있는 경우")
        void password_reset_success_update() {
            // given
            temporaryPassword = TemporaryPasswordFixture.create(user, "previousPassword", LocalDateTime.now().plusMinutes(2));
            PasswordResetRequest passwordResetRequest = new PasswordResetRequest(email);

            String previousPassword = temporaryPassword.getPassword();
            LocalDateTime previousExpiresAt = temporaryPassword.getExpiresAt();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.of(temporaryPassword));
            given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);

            ArgumentCaptor<String> temporaryPasswordArgCaptor =
                    ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> expiresAtArgCaptor =
                    ArgumentCaptor.forClass(String.class);

            // when
            authService.issueTemporaryPassword(passwordResetRequest);

            // then
            then(userRepository).should().findByEmail(email);
            then(temporaryPasswordRepository).should().findByUserId(userId);
            then(passwordEncoder).should().encode(anyString());
            then(temporaryPasswordRepository).should(never()).save(any(TemporaryPassword.class));
            then(mailService).should().sendTemporaryPassword(eq(email), temporaryPasswordArgCaptor.capture(), expiresAtArgCaptor.capture());

            assertThat(temporaryPassword.getUser()).isEqualTo(user);
            assertThat(temporaryPassword.getPassword()).isEqualTo(encodedPassword);
            assertThat(temporaryPassword.getPassword()).isNotEqualTo(previousPassword);

            assertThat(temporaryPassword.isExpired()).isFalse();
            assertThat(temporaryPassword.getExpiresAt()).isAfter(previousExpiresAt);

            assertThat(temporaryPasswordArgCaptor.getValue()).isNotBlank();
            assertThat(expiresAtArgCaptor.getValue()).isNotBlank();
        }
        
        @Test
        @DisplayName("실패 - 유저가 없는 경우")
        void password_reset_failed_userNotFound() {
            // given
            PasswordResetRequest request = new PasswordResetRequest(email);
            given(userRepository.findByEmail(email))
                    .willReturn(Optional.empty());
            
            // when
            assertThatThrownBy(() -> authService.issueTemporaryPassword(request))
                    .isInstanceOf(UserNotFoundException.class);
            
            // then
            then(userRepository).should().findByEmail(email);
            then(temporaryPasswordRepository).shouldHaveNoInteractions();
            then(passwordEncoder).shouldHaveNoInteractions();
            then(mailService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패 - 메일 발송 실패")
        void password_reset_failed_mailSend() {
            // given
            PasswordResetRequest request = new PasswordResetRequest(email);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(temporaryPasswordRepository.findByUserId(userId)).willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);

            willThrow(new TemporaryPasswordMailSendFailedException())
                    .given(mailService)
                    .sendTemporaryPassword(eq(email), anyString(), anyString());

            // when & then
            assertThatThrownBy(() -> authService.issueTemporaryPassword(request))
                    .isInstanceOf(TemporaryPasswordMailSendFailedException.class);

            // then
            then(userRepository).should().findByEmail(email);
            then(temporaryPasswordRepository).should().findByUserId(userId);
            then(passwordEncoder).should().encode(anyString());
            then(mailService).should().sendTemporaryPassword(eq(email), anyString(), anyString());
        }
    }

}