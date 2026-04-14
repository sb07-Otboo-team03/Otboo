package com.codeit.otboo.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @NotBlank
    @Size(min = 2, max = 20)
    String name,

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 6)
    String password
) {
}
