package com.codeit.otboo.global.security.jwt.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisRegistryImpl implements RedisRegistry {

    private static final String LOGIN_STATE_KEY_PREFIX = "auth:login:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(UUID userId, String sessionId, String refreshToken, long ttlSeconds) {
        UserInfo userInfo = new UserInfo(sessionId, refreshToken);
        redisTemplate.opsForValue().set(
                key(userId),
                serialize(userInfo),
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public boolean isValidSession(UUID userId, String sessionId) {
        UserInfo userInfo = get(userId);
        return userInfo != null
                && sessionId != null
                && sessionId.equals(userInfo.sessionId());
    }

    @Override
    public boolean isValidRefreshToken(UUID userId, String refreshToken) {
        UserInfo loginState = get(userId);
        return loginState != null
                && refreshToken != null
                && refreshToken.equals(loginState.refreshToken());
    }

    @Override
    public void rotateRefreshToken(UUID userId, String oldRefreshToken, String newRefreshToken, long ttlSeconds) {
        UserInfo current = get(userId);

        if (current == null || !oldRefreshToken.equals(current.refreshToken())) {
            throw new IllegalStateException("현재 유효한 refresh token이 아닙니다.");
        }

        UserInfo updated = new UserInfo(current.sessionId(), newRefreshToken);

        redisTemplate.opsForValue().set(
                key(userId),
                serialize(updated),
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void refreshSession(UUID userId, String sessionId, long ttlSeconds) {
        UserInfo current = get(userId);

        if (current == null || !sessionId.equals(current.sessionId())) {
            throw new IllegalStateException("현재 유효한 session이 아닙니다.");
        }

        redisTemplate.opsForValue().set(
                key(userId),
                serialize(current),
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void delete(UUID userId) {
        redisTemplate.delete(key(userId));
    }

    @Override
    public UserInfo get(UUID userId) {
        String value = redisTemplate.opsForValue().get(key(userId));
        if (value == null) {
            return null;
        }
        return deserialize(value);
    }

    private String key(UUID userId) {
        return LOGIN_STATE_KEY_PREFIX + userId;
    }

    private String serialize(UserInfo loginState) {
        try {
            return objectMapper.writeValueAsString(loginState);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("로그인 상태 직렬화에 실패했습니다.", e);
        }
    }

    private UserInfo deserialize(String value) {
        try {
            return objectMapper.readValue(value, UserInfo.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("로그인 상태 역직렬화에 실패했습니다.", e);
        }
    }
}