package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.dto.request.SignInRequest;
import com.codeit.otboo.global.security.jwt.dto.JwtInformation;

public interface AuthService {
    JwtInformation signIn(SignInRequest signInRequest);

    JwtInformation refreshToken(String refreshToken);
}
