package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.global.security.dto.JwtResponse;

public interface AuthService {
    JwtResponse signIn(String email, String password);
}
