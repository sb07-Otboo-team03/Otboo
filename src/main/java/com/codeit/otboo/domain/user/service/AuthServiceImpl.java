package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.security.dto.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;

    // TODO: JWT 구현 이후 수정 예정 (가짜 JWT access token)
    private static final String jwtAccessToken = "abcdefg";

    @Override
    public JwtResponse signIn(String email, String password) {
        System.out.println("HASH: " + new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password123!"));
        Authentication authRequest = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        OtbooUserDetails userDetails = (OtbooUserDetails) authentication.getPrincipal();
        UserResponse userResponse = userDetails.getUserResponse();
        return new JwtResponse(
            userResponse, jwtAccessToken
        );

    }
}
