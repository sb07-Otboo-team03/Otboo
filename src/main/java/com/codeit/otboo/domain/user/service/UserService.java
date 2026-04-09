package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.user.dto.request.*;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.slice.dto.CursorResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User getUser(UUID userId);

    List<User> getAllUsers();

    List<User> getAllUserByIds(List<UUID> userIds);

    UserResponse createUser(UserCreateRequest userCreateRequest);

    ProfileResponse getProfile(UUID userId);

    void updateUserPassword(UUID userId, UpdatePasswordRequest updatePasswordRequest);

    CursorResponse<UserResponse> getAllUsers(UserSearchRequest request);

    ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest profileUpdateRequest, BinaryContentCreateRequest imageRequest);

    UserResponse updateUserLockStatus(UUID userId, UserLockUpdateRequest userLockUpdateRequest);

    UserResponse updateUserRole(UUID userId, UserRoleUpdateRequest userRoleUpdateRequest);
}

