package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.repository.ProfileRepository;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserEmailDuplicateException;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        boolean exists = userRepository.existsByEmail(userCreateRequest.email());
        if (exists) {
            throw new UserEmailDuplicateException(userCreateRequest.email());
        }
        String encodedPassword = passwordEncoder.encode(userCreateRequest.password());

        User user = User.builder()
                .email(userCreateRequest.email())
                .password(encodedPassword)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .name(userCreateRequest.name())
                .build();

        user.setProfile(profile);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public CursorResponse<UserResponse> getAllFeed(FeedSearchRequest request, UUID authorIdEqual) {
        return null;
    }

    @Override
    public UserResponse updateUserRole(UUID userId, Role role) {
        return null;
    }

    @Override
    public ProfileResponse getProfile(UUID userId) {
        return null;
    }
}
