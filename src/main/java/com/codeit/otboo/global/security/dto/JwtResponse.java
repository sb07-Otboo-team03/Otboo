package com.codeit.otboo.global.security.dto;

import com.codeit.otboo.domain.user.dto.response.UserResponse;

public record JwtResponse(
        UserResponse userDto,
        String accessToken
) {
}
