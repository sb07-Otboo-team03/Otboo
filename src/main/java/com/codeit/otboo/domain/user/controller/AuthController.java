package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.user.service.AuthService;
import com.codeit.otboo.global.security.jwt.JwtProperties;
import com.codeit.otboo.global.security.jwt.RefreshCookieFactory;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;
import com.codeit.otboo.global.security.jwt.dto.JwtResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;
    private final JwtProperties jwtProperties;

    @PostMapping(value = "/sign-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JwtResponse> signIn(@RequestPart("username") String username,
                                              @RequestPart("password") String password,
                                              HttpServletResponse response) {
        JwtInformation jwtInformation = authService.signIn(username, password);
        long refreshTokenExpirationSeconds =
                TimeUnit.MILLISECONDS.toSeconds(
                        jwtProperties.refreshTokenExpiration()
                );

        Cookie cookie = refreshCookieFactory.create(
                jwtInformation.refreshToken(),
                refreshTokenExpirationSeconds
        );

        response.addCookie(cookie);
        JwtResponse jwtResponse = new JwtResponse(jwtInformation.userResponse(), jwtInformation.accessToken());
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@CookieValue("REFRESH_TOKEN") String refreshToken, HttpServletResponse response) {
        log.debug("refreshToken : {}", refreshToken);
        JwtInformation jwtInformation = authService.refreshToken(refreshToken);

        long refreshTokenExpirationSeconds =
                TimeUnit.MILLISECONDS.toSeconds(
                        jwtProperties.refreshTokenExpiration()
                );

        Cookie cookie = refreshCookieFactory.create(
                jwtInformation.refreshToken(),
                refreshTokenExpirationSeconds
        );

        response.addCookie(cookie);
        JwtResponse jwtResponse = new JwtResponse(jwtInformation.userResponse(), jwtInformation.accessToken());
        return ResponseEntity.ok(jwtResponse);

    }
}
