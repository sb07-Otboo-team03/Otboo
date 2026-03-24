package com.codeit.otboo.global.security.jwt.registry;

import java.util.UUID;

public interface LoginSessionRegistry {

    void save(UUID userId, String sessionId, long ttlSeconds);

    boolean isValid(UUID userId, String sessionId);

    void delete(UUID userId);
}