package com.codeit.otboo.domain.user.dto.request;

import com.codeit.otboo.domain.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
        @NotNull
        Role role
) {
}
