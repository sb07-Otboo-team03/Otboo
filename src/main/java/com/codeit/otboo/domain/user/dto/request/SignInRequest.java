package com.codeit.otboo.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequest(
        @Email
        String username,

        @NotBlank
        @Size(min = 6)
        String password
) {}
