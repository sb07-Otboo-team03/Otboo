package com.codeit.otboo.global.security.jwt;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieFactory {

    public Cookie create(String refreshToken, long maxAgeSeconds) {
        Cookie cookie = new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) maxAgeSeconds);
        return cookie;
    }
}
