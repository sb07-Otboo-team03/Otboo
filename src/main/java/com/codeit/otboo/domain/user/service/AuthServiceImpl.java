package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtProperties;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;
import com.codeit.otboo.global.security.jwt.JwtProvider;
import com.codeit.otboo.global.security.jwt.exception.JwtExpiredTokenException;
import com.codeit.otboo.global.security.jwt.registry.LoginSessionRegistry;
import com.codeit.otboo.global.security.jwt.registry.RefreshTokenRegistry;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRegistry refreshTokenRegistry;
    private final LoginSessionRegistry loginSessionRegistry;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;

    @Override
    public JwtInformation signIn(String email, String password) {
        Authentication authRequest = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(authRequest);

        OtbooUserDetails userDetails = (OtbooUserDetails) authentication.getPrincipal();
        UserResponse userResponse = userDetails.getUserResponse();

        UUID userId = userResponse.id();
        String authenticatedEmail = userResponse.email();
        String sessionId = UUID.randomUUID().toString();

        String accessToken = jwtProvider.generateAccessToken(userId, authenticatedEmail, sessionId);
        String refreshToken = jwtProvider.generateRefreshToken(userId, authenticatedEmail, sessionId);

        loginSessionRegistry.save(
                userId,
                sessionId,
                jwtProperties.refreshTokenExpiration()
        );

        refreshTokenRegistry.register(
                userId,
                refreshToken,
                jwtProperties.refreshTokenExpiration()
        );

        return JwtInformation.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userResponse(userResponse)
                .build();
    }

    @Override
    public JwtInformation refreshToken(String refreshToken) {

        // JWT 자체 검증
        JWTClaimsSet claims = jwtProvider.validateRefreshToken(refreshToken);

        if (!refreshTokenRegistry.isValidRefreshToken(refreshToken)) {
            throw new JwtExpiredTokenException();
        }

        UUID userId = UUID.fromString(claims.getSubject());
        String sessionId = jwtProvider.getSessionId(refreshToken);
        String email = jwtProvider.getEmail(refreshToken);

        String newAccessToken = jwtProvider.generateAccessToken(userId, email, sessionId);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId, email, sessionId);

        refreshTokenRegistry.rotate(userId, refreshToken, newRefreshToken, jwtProperties.refreshTokenExpiration());
        loginSessionRegistry.save(userId, sessionId, jwtProperties.refreshTokenExpiration());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserResponse userResponse = userMapper.toDto(user);

        return JwtInformation.builder()
                .userResponse(userResponse)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
