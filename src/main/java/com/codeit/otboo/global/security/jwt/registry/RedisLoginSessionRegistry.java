package com.codeit.otboo.global.security.jwt.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisLoginSessionRegistry implements LoginSessionRegistry {

    private static final String LOGIN_SESSION_KEY_PREFIX = "auth:session:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(UUID userId, String sessionId, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                LOGIN_SESSION_KEY_PREFIX + userId,
                sessionId,
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public boolean isValid(UUID userId, String sessionId) {
        String currentSessionId = redisTemplate.opsForValue()
                .get(LOGIN_SESSION_KEY_PREFIX + userId);

        return sessionId != null && sessionId.equals(currentSessionId);
    }

    @Override
    public void delete(UUID userId) {
        redisTemplate.delete(LOGIN_SESSION_KEY_PREFIX + userId);
    }
}