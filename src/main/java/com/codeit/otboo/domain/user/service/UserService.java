package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.global.slice.dto.CursorResponse;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserCreateRequest userCreateRequest);

    CursorResponse<UserResponse> getAllFeed(FeedSearchRequest request, UUID authorIdEqual);

    UserResponse updateUserRole(UUID userId, Role role);

    ProfileResponse getProfile(UUID userId);


}
