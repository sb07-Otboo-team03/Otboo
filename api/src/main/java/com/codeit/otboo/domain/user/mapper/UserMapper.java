package com.codeit.otboo.domain.user.mapper;

import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.feed.dto.response.AuthorResponse;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import com.codeit.otboo.domain.user.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final BinaryContentUrlResolver urlResolver;

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

        String url = binaryContentId != null ?
                urlResolver.resolve(binaryContentId) : null;

        return UserSummaryResponse.builder()
            .userId(userId)
            .name(name)
            .profileImageUrl(url)
            .build();
    }

    public AuthorResponse toAuthorDto(UUID userId, String name, UUID binaryContentId) {

        String url = binaryContentId != null ?
                urlResolver.resolve(binaryContentId) : null;

        return AuthorResponse.builder()
                .userId(userId)
                .name(name)
                .profileImageUrl(url)
                .build();
    }
}
