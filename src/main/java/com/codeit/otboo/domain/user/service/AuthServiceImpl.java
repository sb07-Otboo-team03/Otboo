package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.dto.PasswordResetRequest;
import com.codeit.otboo.domain.user.dto.request.SignInRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.TemporaryPassword;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.AuthStatePersistentException;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.TemporaryPasswordRepository;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtProperties;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;
import com.codeit.otboo.global.security.jwt.JwtProvider;
import com.codeit.otboo.global.security.jwt.exception.JwtExpiredTokenException;
import com.codeit.otboo.global.security.jwt.registry.RedisRegistry;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisRegistry redisRegistry;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordRepository temporaryPasswordRepository;
    private final UserDetailsService userDetailsService;
    private final MailService mailService;

    @Override
    @Transactional
    public JwtInformation signIn(SignInRequest signInRequest) {
        String email = signInRequest.username();
        String password = signInRequest.password();
        UserResponse userResponse;

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("자격 증명에 실패하였습니다."));

        TemporaryPassword temporaryPassword = temporaryPasswordRepository.findByUserId(user.getId())
                .filter(TemporaryPassword::isValid)
                .orElse(null);

        if (temporaryPassword != null) {
            if (!passwordEncoder.matches(password, temporaryPassword.getPassword())) {
                throw new BadCredentialsException("자격 증명에 실패하였습니다.");
            }
            OtbooUserDetails userDetails =
                    (OtbooUserDetails) userDetailsService.loadUserByUsername(email);

            if(!userDetails.isAccountNonLocked()){
                throw new LockedException("잠김 계정입니다.");
            }

            userResponse = userDetails.getUserResponse();
        } else {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(email, password);
            Authentication authentication = authenticationManager.authenticate(authRequest);

            OtbooUserDetails userDetails = (OtbooUserDetails) authentication.getPrincipal();
            userResponse = userDetails.getUserResponse();
        }


        UUID userId = userResponse.id();
        String authenticatedEmail = userResponse.email();
        String sessionId = UUID.randomUUID().toString();

        String refreshToken = jwtProvider.generateRefreshToken(userId, authenticatedEmail, sessionId);

        try {
            redisRegistry.save(userId, sessionId, refreshToken, jwtProperties.refreshTokenExpiration());
        } catch (Exception e) {
            // Redis 저장 중, 예외가 발생한다면 해당 key 삭제
            redisRegistry.delete(userId);
            throw new AuthStatePersistentException();
        }

        String accessToken = jwtProvider.generateAccessToken(userId, authenticatedEmail, sessionId);

        return JwtInformation.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userResponse(userResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public JwtInformation refreshToken(String refreshToken) {

        // JWT 자체 검증
        JWTClaimsSet claims = jwtProvider.validateRefreshToken(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());

        // Redis에 저장된 user의 refresh Token 비교
        if (!redisRegistry.isValidRefreshToken(userId, refreshToken)) {
            throw new JwtExpiredTokenException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String sessionId = jwtProvider.getSessionId(refreshToken);
        String email = jwtProvider.getEmail(refreshToken);

        String newRefreshToken = jwtProvider.generateRefreshToken(userId, email, sessionId);

        try {
            redisRegistry.rotateRefreshToken(userId, refreshToken, newRefreshToken, jwtProperties.refreshTokenExpiration());
        } catch (Exception e) {
            redisRegistry.delete(userId);
            throw new AuthStatePersistentException();
        }

        String newAccessToken = jwtProvider.generateAccessToken(userId, email, sessionId);
        UserResponse userResponse = userMapper.toDto(user);

        return JwtInformation.builder()
                .userResponse(userResponse)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void signOut(UUID userId) {
        redisRegistry.delete(userId);
    }

    @Override
    @Transactional
    public void issueTemporaryPassword(PasswordResetRequest passwordResetRequest) {
        String email = passwordResetRequest.email();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        String temporaryPassword = generateTemporaryPassword();
        String encoded = passwordEncoder.encode(temporaryPassword);
        LocalDateTime expiresAt = LocalDateTime.now().plus(Duration.ofMinutes(3));
        temporaryPasswordRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        temporaryEntity -> {
                            temporaryEntity.update(encoded, expiresAt);
                        },
                        () -> temporaryPasswordRepository.save(
                                TemporaryPassword.builder()
                                        .user(user)
                                        .password(encoded)
                                        .expiresAt(expiresAt)
                                        .expired(false)
                                        .build()
                        )
                );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expiresAtToString = expiresAt.format(formatter);
        mailService.sendTemporaryPassword(email, temporaryPassword, expiresAtToString);
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
