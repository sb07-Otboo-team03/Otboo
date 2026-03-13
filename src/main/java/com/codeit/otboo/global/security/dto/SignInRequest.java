package com.codeit.otboo.global.security.dto;

public record SignInRequest(
        String username,
        String password
) {}
