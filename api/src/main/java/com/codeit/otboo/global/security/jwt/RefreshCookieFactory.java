package com.codeit.otboo.global.security.jwt;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieFactory {

    @Value("${cookie.secure:false}")
    private boolean secure;

    @Value("${cookie.same-site:Lax}")
    private String sameSite;

    public Cookie create(String refreshToken, long maxAgeSeconds) {
        Cookie cookie = new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", sameSite);
        cookie.setMaxAge((int) maxAgeSeconds);
        return cookie;
    }

    public Cookie delete() {
        Cookie cookie = new Cookie(JwtProvider.REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", sameSite);
        cookie.setMaxAge(0);
        return cookie;
    }
}
