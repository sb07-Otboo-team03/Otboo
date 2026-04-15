package com.codeit.otboo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank
        @Size(min = 2, max = 20)
        @Schema(description = "이름", example = "홍길동")
        String name,

        @NotBlank
        @Email
        @Schema(description = "이메일", example = "test@email.com")
        String email,

        @NotBlank
        @Size(min = 6)
        @Schema(description = "비밀번호", example = "password123")
        String password
) {
}