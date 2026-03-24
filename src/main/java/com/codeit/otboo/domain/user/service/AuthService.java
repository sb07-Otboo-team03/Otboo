package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.global.security.jwt.dto.JwtInformation;

public interface AuthService {
    JwtInformation signIn(String email, String password);

    JwtInformation refreshToken(String refreshToken);
}
