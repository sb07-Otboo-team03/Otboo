package com.codeit.otboo.global.oauth.handler;

import com.codeit.otboo.domain.user.service.AuthService;
import com.codeit.otboo.global.security.jwt.JwtProperties;
import com.codeit.otboo.global.security.jwt.RefreshCookieFactory;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;
    private final JwtProperties jwtProperties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        JwtInformation jwtInformation = authService.signInByOAuth(email, name);

        long refreshTokenExpirationSeconds =
                TimeUnit.MILLISECONDS.toSeconds(jwtProperties.refreshTokenExpiration());

        Cookie cookie = refreshCookieFactory.create(
                jwtInformation.refreshToken(),
                refreshTokenExpirationSeconds
        );

        response.addCookie(cookie);

        log.debug("OAuth2 refresh cookie 저장 완료. email={}", email);

        response.sendRedirect("/");
    }
}