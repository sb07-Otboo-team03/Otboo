package com.codeit.otboo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequest(
        @Email
        @NotBlank
        @Schema(description = "이메일", example = "email@email.com")
        String username,

        @NotBlank
        @Size(min = 6)
        @Schema(description = "비밀번호", example = "password")
        String password
) {}
