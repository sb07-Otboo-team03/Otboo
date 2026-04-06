package com.codeit.otboo.domain.user.fixture;

import com.codeit.otboo.domain.user.entity.TemporaryPassword;
import com.codeit.otboo.domain.user.entity.User;

import java.time.LocalDateTime;

public class TemporaryPasswordFixture {
    public static TemporaryPassword create(User user, String encodedPassword, LocalDateTime expiresAt) {
        return TemporaryPassword.builder()
                .user(user)
                .password(encodedPassword)
                .expiresAt(expiresAt)
                .expired(false)
                .build();
    }
}
