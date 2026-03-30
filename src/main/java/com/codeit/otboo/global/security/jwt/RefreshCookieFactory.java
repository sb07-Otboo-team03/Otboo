package com.codeit.otboo.global.security.jwt;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieFactory {

    // TODO: 현재는 setSecure를 false로 선택
    // NOTE: 로컬호스트 및 https 환경에서만 쿠키가 작동하는 로직이라, 
    // false로 setSecure 변경하여 http, https 두 환경에서 모두 쿠키 전송 가능 설정
    public Cookie create(String refreshToken, long maxAgeSeconds) {
        Cookie cookie = new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge((int) maxAgeSeconds);
        return cookie;
    }

    public Cookie delete() {
        Cookie cookie = new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge(0);
        return cookie;
    }
}
