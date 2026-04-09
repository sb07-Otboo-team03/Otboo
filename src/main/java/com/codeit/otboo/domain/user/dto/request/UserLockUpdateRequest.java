package com.codeit.otboo.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserLockUpdateRequest(
        @NotNull
        boolean locked
) {
}
