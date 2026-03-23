package com.codeit.otboo.domain.user.mapper;

import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import com.codeit.otboo.domain.user.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    BinaryContentUrlResolver urlResolver;

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

    public UserSummaryResponse toSummaryDto(UUID userId, String name, UUID binaryContentId) {

        return UserSummaryResponse.builder()
            .userId(userId)
            .name(name)
            .profileImageUrl(urlResolver.resolve(binaryContentId))
            .build();
    }
}
