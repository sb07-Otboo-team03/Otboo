package com.codeit.otboo.global.security.jwt;

import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.fixture.UserResponseFixture;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.exception.JwtInvalidTokenTypeException;
import com.codeit.otboo.global.security.jwt.registry.RedisRegistry;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.UUID;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisRegistry redisRegistry;

    @Mock
    UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private AuthenticationEntryPoint authenticationEntryPoint;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private OtbooUserDetails userDetails;
    private UUID userId;
    private String email;
    private String password;
    private String sessionId;
    private JWTClaimsSet claimsSet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@codeit.com";
        password = "password123";
        sessionId = "session-id";
        User user = UserFixture.create(userId, email, password);
        UserResponse userResponse = UserResponseFixture.create(user);
        claimsSet = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .build();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                jwtProvider,
                redisRegistry,
                userDetailsService,
                authenticationEntryPoint
        );

        userDetails = new OtbooUserDetails(userResponse, password);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Jwt 인증 - 유효한 토큰")
    void doFilter_ValidToken_SetsAuthentication() throws Exception {
        // given
        String accessToken = "valid-access-token";
        given(request.getHeader("Authorization")).willReturn("Bearer " + accessToken);
        given(jwtProvider.validateAccessToken(accessToken)).willReturn(claimsSet);
        given(jwtProvider.getSessionId(accessToken)).willReturn(sessionId);
        given(jwtProvider.getEmail(accessToken)).willReturn(email);
        given(redisRegistry.isValidSession(userId, sessionId)).willReturn(true);
        given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider, redisRegistry, userDetailsService, authenticationEntryPoint);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        then(securityContext).should().setAuthentication(any());
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 그대로 다음 필터로 전달한다")
    void doFilter_authorizationHeader_isNull() throws Exception {
        // given
        given(request.getHeader("Authorization")).willReturn(null);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        then(filterChain).should().doFilter(request, response);
        then(jwtProvider).shouldHaveNoInteractions();
        then(redisRegistry).shouldHaveNoInteractions();
        then(userDetailsService).shouldHaveNoInteractions();
        then(securityContext).should(never()).setAuthentication(any());
    }

    @Test
    @DisplayName("Bearer가 유효하지 않으면 그대로 다음 필터로 전달한다")
    void doFilter_Bearer_isNotValid() throws Exception {
        // given
        given(request.getHeader("Authorization")).willReturn("Bearerrrrr");

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        then(filterChain).should().doFilter(request, response);
        then(jwtProvider).shouldHaveNoInteractions();
        then(redisRegistry).shouldHaveNoInteractions();
        then(userDetailsService).shouldHaveNoInteractions();
        then(securityContext).should(never()).setAuthentication(any());
    }

    @Test
    @DisplayName("access 토큰이 유효하지 않으면 예외를 발생한다")
    void doFilter_accessToken_isNotValid() throws Exception {
        // given
        String accessToken = "invalid-access-token";
        given(request.getHeader("Authorization")).willReturn("Bearer " + accessToken);
        given(jwtProvider.validateAccessToken(accessToken))
                .willThrow(new JwtInvalidTokenTypeException());

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        then(authenticationEntryPoint).should().commence(
                eq(request),
                eq(response),
                any(BadCredentialsException.class)
        );
        then(userDetailsService).shouldHaveNoInteractions();
        then(filterChain).should(never()).doFilter(request, response);
        then(securityContext).should(never()).setAuthentication(any());
    }

    @Test
    @DisplayName("Session Id가 유효하지 않으면, 예외가 발생한다.")
    void doFilter_sessionId_isNotValid() throws Exception {
        String accessToken = "valid-access-token";
        given(request.getHeader("Authorization")).willReturn("Bearer " + accessToken);
        given(jwtProvider.validateAccessToken(accessToken)).willReturn(claimsSet);
        given(jwtProvider.getSessionId(accessToken)).willReturn(sessionId);
        given(jwtProvider.getEmail(accessToken)).willReturn(email);
        given(redisRegistry.isValidSession(userId, sessionId)).willReturn(false);


        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        then(authenticationEntryPoint).should().commence(
                eq(request),
                eq(response),
                any(BadCredentialsException.class)
        );
        then(userDetailsService).shouldHaveNoInteractions();
        then(filterChain).should(never()).doFilter(request, response);
        then(securityContext).should(never()).setAuthentication(any());
    }

    @Test
    @DisplayName("사용자 조회에 실패하면 UsernameNotFoundException이 전파된다")
    void doFilter_user_notfound() throws Exception {
        // given
        String accessToken = "valid-access-token";
        given(request.getHeader("Authorization")).willReturn("Bearer " + accessToken);
        given(jwtProvider.validateAccessToken(accessToken)).willReturn(claimsSet);
        given(jwtProvider.getSessionId(accessToken)).willReturn(sessionId);
        given(jwtProvider.getEmail(accessToken)).willReturn(email);
        given(redisRegistry.isValidSession(userId, sessionId)).willReturn(true);
        given(userDetailsService.loadUserByUsername(email))
                .willThrow(new UsernameNotFoundException("user not found"));

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        then(authenticationEntryPoint).should().commence(
                eq(request),
                eq(response),
                any(UsernameNotFoundException.class)
        );
        then(filterChain).should(never()).doFilter(request, response);
        then(securityContext).should(never()).setAuthentication(any());
    }


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

}