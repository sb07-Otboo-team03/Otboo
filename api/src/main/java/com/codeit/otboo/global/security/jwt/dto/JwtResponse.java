package com.codeit.otboo.global.security.jwt.dto;

import com.codeit.otboo.domain.user.dto.response.UserResponse;

public record JwtResponse(
        UserResponse userDto,
        String accessToken
) {
}
