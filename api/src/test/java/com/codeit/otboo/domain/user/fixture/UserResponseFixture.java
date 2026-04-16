package com.codeit.otboo.domain.user.fixture;

import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;

public class UserResponseFixture {
    public static UserResponse create(User user) {
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .email(user.getEmail())
                .name("test")
                .role(user.getRole())
                .locked(false)
                .build();
        return userResponse;
    }

    public static UserResponse create(User user, boolean locked) {
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .email(user.getEmail())
                .name("test")
                .role(user.getRole())
                .locked(locked)
                .build();
        return userResponse;
    }

    public static UserResponse create(User user, Role role) {
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .email(user.getEmail())
                .name("test")
                .role(role)
                .locked(user.isLocked())
                .build();
        return userResponse;
    }
}
