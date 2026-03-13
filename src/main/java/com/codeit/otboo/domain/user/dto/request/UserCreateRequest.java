package com.codeit.otboo.domain.user.dto.request;

public record UserCreateRequest(
    String name,
    String email,
    String password
) {
}
