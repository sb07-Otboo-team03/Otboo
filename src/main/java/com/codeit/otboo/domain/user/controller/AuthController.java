package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.user.controller.docs.AuthControllerDocs;
import com.codeit.otboo.domain.user.dto.request.PasswordResetRequest;
import com.codeit.otboo.domain.user.dto.request.SignInRequest;
import com.codeit.otboo.domain.user.service.AuthService;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.jwt.JwtProperties;
import com.codeit.otboo.global.security.jwt.RefreshCookieFactory;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;
import com.codeit.otboo.global.security.jwt.dto.JwtResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController implements AuthControllerDocs {
    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;
    private final JwtProperties jwtProperties;

    @PostMapping(value = "/sign-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JwtResponse> signIn(@Valid @ModelAttribute SignInRequest signInRequest,
                                              HttpServletResponse response) {
        JwtInformation jwtInformation = authService.signIn(signInRequest);
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

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(@AuthenticationPrincipal OtbooUserDetails userDetails, HttpServletResponse response) {
        UUID userId = userDetails.getUserResponse().id();
        authService.signOut(userId);

        Cookie deleteCookie = refreshCookieFactory.delete();
        response.addCookie(deleteCookie);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> passwordReset(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        authService.issueTemporaryPassword(passwordResetRequest);
        return ResponseEntity.noContent().build();
    }

}
