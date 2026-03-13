package com.codeit.otboo.domain.user.dto.response;

import com.codeit.otboo.domain.user.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        LocalDateTime createdAt,
        String email,
        String name,
        Role role,
        boolean locked
) {
}
