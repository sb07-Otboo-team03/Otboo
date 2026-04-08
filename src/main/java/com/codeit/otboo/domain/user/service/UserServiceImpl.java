package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.dto.request.UpdatePasswordRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserSearchCondition;
import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserEmailDuplicateException;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.mapper.ProfileMapper;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.TemporaryPasswordRepository;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;
    private final BinaryContentUrlResolver binaryContentUrlResolver;
    private final TemporaryPasswordRepository temporaryPasswordRepository;

    @Override
    @Transactional(readOnly = true)
    public User getUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getAllUserByIds(List<UUID> userIds) {
        return userRepository.findAllById(userIds);
    }


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
    public UserResponse updateUserRole(UUID userId, Role role) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId));
        String profileImageUrl = null;
        BinaryContent binaryContent = user.getProfile().getBinaryContent();
        if (binaryContent!= null) {
            UUID binaryContentId = binaryContent.getId();
                profileImageUrl = binaryContentUrlResolver.resolve(binaryContentId);
        }
        return profileMapper.toDto(user, profileImageUrl);
    }

    @Override
    @Transactional
    @PreAuthorize("#userId == authentication.principal.userResponse.id")
    public void updateUserPassword(UUID userId, UpdatePasswordRequest updatePasswordRequest) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId));

        String encodedPassword = passwordEncoder.encode(updatePasswordRequest.password());
        user.updatePassword(encodedPassword);

        // 임시 비밀번호 삭제
        temporaryPasswordRepository.deleteByUserId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CursorResponse<UserResponse> getAllUsers(UserSearchRequest request) {

        UserSearchCondition condition = UserSearchCondition.from(request);

        Slice<User> userPage = userRepository.findAllByKeywordLike(condition);
        List<User> content = userPage.getContent();

        if (content.isEmpty())
            return new CursorResponse<>(List.of(), null, null,
                    false, 0L, request.sortBy(), request.sortDirection());

        long totalCount = userRepository.countTotalElements(condition);

        List<UserResponse> data = content.stream()
                .map(userMapper::toDto).toList();

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (userPage.hasNext()) {
            User lastUser = content.get(data.size() - 1);

            nextCursor = request.sortBy().equals("createdAt") ?
                    String.valueOf(lastUser.getCreatedAt()) :
                    String.valueOf(lastUser.getEmail());
            nextIdAfter = lastUser.getId();
        }

        return new CursorResponse<>(data, nextCursor, nextIdAfter,
                userPage.hasNext(), totalCount, request.sortBy(), request.sortDirection());
    }
}
