package com.codeit.otboo.domain.user.dto.request;

import com.codeit.otboo.domain.user.entity.Role;

public record UserRoleUpdateRequest(
        Role role
) {
}
