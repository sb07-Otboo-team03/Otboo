package com.codeit.otboo.domain.user.fixture;

import com.codeit.otboo.domain.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserFixture {
    public static User create(UUID userId, String email, String password) {
        User user = User.builder()
                .email(email)
                .password(password)
                .build();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        ReflectionTestUtils.setField(user,"createdAt", createdAt);
        ReflectionTestUtils.setField(user,"id", userId);

        return user;
    }
}
