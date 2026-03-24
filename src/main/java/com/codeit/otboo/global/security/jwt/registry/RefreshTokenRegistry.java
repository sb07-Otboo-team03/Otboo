package com.codeit.otboo.global.security.jwt.registry;


import java.util.UUID;

public interface RefreshTokenRegistry {

    void register(UUID userId, String refreshToken, long ttlSeconds);

    boolean isValidRefreshToken(String refreshToken);

    void rotate(UUID userId, String oldRefreshToken, String newRefreshToken, long ttlSeconds);

    void logout(UUID userId);
}