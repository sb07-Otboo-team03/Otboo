package com.codeit.otboo.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        @NotBlank
        @Size(min = 6)
        String password
) {
}
