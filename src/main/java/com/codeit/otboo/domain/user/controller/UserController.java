package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.mapper.BinaryContentMapper;
import com.codeit.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.user.dto.request.*;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final BinaryContentMapper binaryContentMapper;

    @PostMapping
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        UserResponse userResponse = userService.createUser(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/{userId}/profiles")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable("userId") UUID userId) {
        ProfileResponse profileResponse = userService.getProfile(userId);
        return ResponseEntity.ok(profileResponse);
    }


    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable("userId") UUID userId, @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        userService.updateUserPassword(userId, updatePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CursorResponse<UserResponse>> getAllUser(@Valid @ParameterObject @ModelAttribute UserSearchRequest request) {
        CursorResponse<UserResponse> getUsers = userService.getAllUsers(request);
        return ResponseEntity.ok(getUsers);
    }

    @PatchMapping(value = "/{userId}/profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestPart ProfileUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        BinaryContentCreateRequest binaryContentCreateRequest = binaryContentMapper.toRequestDto(image);
        ProfileResponse profileResponse = userService.updateProfile(userId, request, binaryContentCreateRequest);
        return ResponseEntity.ok(profileResponse);
    }

    @PatchMapping(value = "/{userId}/lock")
    public ResponseEntity<UserResponse> updateUserLock(@PathVariable UUID userId, @Valid @RequestBody UserLockUpdateRequest userLockUpdateRequest) {
        UserResponse userResponse = userService.updateUserLockStatus(userId, userLockUpdateRequest);
        return ResponseEntity.ok(userResponse);
    }

    @PatchMapping(value = "/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable UUID userId, @Valid @RequestBody UserRoleUpdateRequest userRoleUpdateRequest) {
        UserResponse userResponse = userService.updateUserRole(userId, userRoleUpdateRequest);
        return ResponseEntity.ok(userResponse);
    }


}
