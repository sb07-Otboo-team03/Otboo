package com.codeit.otboo.global.security.jwt.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRefreshTokenRegistry implements RefreshTokenRegistry {

    private static final String USER_REFRESH_KEY_PREFIX = "jwt:user:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "jwt:refresh:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void register(UUID userId, String refreshToken, long ttlSeconds) {
        String userKey = USER_REFRESH_KEY_PREFIX + userId;
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;

        String oldRefreshToken = redisTemplate.opsForValue().get(userKey);
        if (oldRefreshToken != null) {
            redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + oldRefreshToken);
        }

        redisTemplate.opsForValue().set(userKey, refreshToken, ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(refreshKey, userId.toString(), ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean isValidRefreshToken(String refreshToken) {
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;

        String userIdStr = redisTemplate.opsForValue().get(refreshKey);
        if (userIdStr == null) {
            return false;
        }

        String userKey = USER_REFRESH_KEY_PREFIX + userIdStr;
        String currentRefreshToken = redisTemplate.opsForValue().get(userKey);

        return refreshToken.equals(currentRefreshToken);
    }

    @Override
    public void rotate(UUID userId, String oldRefreshToken, String newRefreshToken, long ttlSeconds) {
        String userKey = USER_REFRESH_KEY_PREFIX + userId;
        String currentRefreshToken = redisTemplate.opsForValue().get(userKey);

        if (currentRefreshToken == null || !currentRefreshToken.equals(oldRefreshToken)) {
            throw new IllegalStateException("현재 유효한 refresh token이 아닙니다.");
        }

        redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + oldRefreshToken);
        register(userId, newRefreshToken, ttlSeconds);
    }

    @Override
    public void logout(UUID userId) {
        String userKey = USER_REFRESH_KEY_PREFIX + userId;
        String refreshToken = redisTemplate.opsForValue().get(userKey);

        if (refreshToken != null) {
            redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + refreshToken);
        }

        redisTemplate.delete(userKey);
    }
}