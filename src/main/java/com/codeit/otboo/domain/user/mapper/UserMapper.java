package com.codeit.otboo.domain.user.mapper;

import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import com.codeit.otboo.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .email(user.getEmail())
                .name(user.getProfile().getName())
                .role(user.getRole())
                .locked(user.isLocked())
                .build();
    }

    public UserSummaryResponse toSummaryDto(User user, String imageUrl) {
        return UserSummaryResponse.builder()
                .userId(user.getId())
                .name(user.getProfile().getName())
                .profileImageUrl(imageUrl) // TODO: S3 코드 작성 시점에 작성할 예정
                .build();
    }
}
