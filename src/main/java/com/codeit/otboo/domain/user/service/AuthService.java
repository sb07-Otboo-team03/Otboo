package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.dto.PasswordResetRequest;
import com.codeit.otboo.domain.user.dto.request.SignInRequest;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;

import java.util.UUID;

public interface AuthService {
    JwtInformation signIn(SignInRequest signInRequest);

    JwtInformation refreshToken(String refreshToken);

    void signOut(UUID userId);

    void issueTemporaryPassword(PasswordResetRequest passwordResetRequest);
}
