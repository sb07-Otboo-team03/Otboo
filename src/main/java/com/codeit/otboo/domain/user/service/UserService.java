package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.user.dto.request.UpdatePasswordRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.global.slice.dto.CursorResponse;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserCreateRequest userCreateRequest);

    UserResponse updateUserRole(UUID userId, Role role);

    ProfileResponse getProfile(UUID userId);

    void updateUserPassword(UUID userId, UpdatePasswordRequest updatePasswordRequest);

    CursorResponse<UserResponse> getAllUsers(UserSearchRequest request);
}
