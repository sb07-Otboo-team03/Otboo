package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.user.dto.request.UpdatePasswordRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

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
    public ResponseEntity<CursorResponse<UserResponse>> getCurrentUser(@ParameterObject @ModelAttribute UserSearchRequest request) {
        CursorResponse<UserResponse> getUsers = userService.getAllUsers(request);
        return ResponseEntity.ok(getUsers);

    }
}
