package com.codeit.otboo.global.security.jwt.dto;

import com.codeit.otboo.domain.user.dto.response.UserResponse;
import lombok.Builder;

@Builder
public record JwtInformation(
        UserResponse userResponse,
        String accessToken,
        String refreshToken
) {
}