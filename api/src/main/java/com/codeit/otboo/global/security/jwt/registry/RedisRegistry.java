package com.codeit.otboo.global.security.jwt.registry;

import java.util.UUID;

public interface RedisRegistry {

    void save(UUID userId, String sessionId, String refreshToken, long ttlSeconds);

    boolean isValidSession(UUID userId, String sessionId);

    boolean isValidRefreshToken(UUID userId, String refreshToken);

    void rotateRefreshToken(UUID userId, String oldRefreshToken, String newRefreshToken, long ttlSeconds);

    void delete(UUID userId);

    UserInfo get(UUID userId);
}