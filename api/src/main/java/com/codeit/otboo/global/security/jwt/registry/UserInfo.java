package com.codeit.otboo.global.security.jwt.registry;

public record UserInfo(
        String sessionId,
        String refreshToken
) {
}
