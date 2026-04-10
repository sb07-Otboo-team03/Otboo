package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.domain.profile.dto.request.LocationRequest;
import com.codeit.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Location;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.sse.event.UserRoleUpdatedEvent;
import com.codeit.otboo.domain.user.dto.request.*;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserEmailDuplicateException;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.mapper.ProfileMapper;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.TemporaryPasswordRepository;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.security.jwt.registry.event.SessionDeletedRequestEvent;
import com.codeit.otboo.global.security.jwt.registry.event.SessionInvalidationReason;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;
    private final BinaryContentUrlResolver binaryContentUrlResolver;
    private final TemporaryPasswordRepository temporaryPasswordRepository;
    private final BinaryContentService binaryContentService;
    private final ApplicationEventPublisher eventPublisher;


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
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId));
        String profileImageUrl = null;
        BinaryContent binaryContent = user.getProfile().getBinaryContent();
        if (binaryContent != null) {
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

    @Override
    @Transactional
    @PreAuthorize("#userId == authentication.principal.userResponse.id")
    public ProfileResponse updateProfile(
            UUID userId,
            ProfileUpdateRequest profileUpdateRequest,
            BinaryContentCreateRequest imageRequest) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Profile profile = user.getProfile();
        BinaryContent oldBinaryContent = profile.getBinaryContent();
        BinaryContent binaryContent = oldBinaryContent;
        BinaryContent newBinaryContent;

        if (imageRequest != null) {
            // TODO: 지혜님 코드 수정에 따라, 삭제할수도 있는 코드
            if (oldBinaryContent != null) {
                binaryContentService.delete(oldBinaryContent.getId());
            }
            newBinaryContent = binaryContentService.upload(imageRequest);
            binaryContent = newBinaryContent;
        }

        LocationRequest locationRequest = profileUpdateRequest.location();
        Location location = profile.getLocation();
        if (locationRequest != null) {
            location = Location.builder()
                    .x(locationRequest.x())
                    .y(locationRequest.y())
                    .latitude(locationRequest.latitude())
                    .longitude(locationRequest.longitude())
                    .region1depthName(locationRequest.locationNames().get(0))
                    .region2depthName(locationRequest.locationNames().get(1))
                    .region3depthName(locationRequest.locationNames().get(2))
                    .region4depthName(locationRequest.locationNames().get(3))
                    .build();
        }

        profile.update(
                profileUpdateRequest.name(),
                profileUpdateRequest.gender(),
                profileUpdateRequest.birthDate(),
                location,
                profileUpdateRequest.temperatureSensitivity(),
                binaryContent
        );


        return profileMapper.toDto(user, resolveImageUrl(binaryContent));
    }

    @Override
    @Transactional
    public UserResponse updateUserLockStatus(UUID userId, UserLockUpdateRequest userLockUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (user.isLocked() != userLockUpdateRequest.locked()) {
            user.updateLockStatus(userLockUpdateRequest.locked());

            if (user.isLocked()) {
                eventPublisher.publishEvent(new SessionDeletedRequestEvent(
                        userId,
                        SessionInvalidationReason.ACCOUNT_LOCKED
                ));
            }
        }
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(UUID userId, UserRoleUpdateRequest userRoleUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Role beforeRole = user.getRole();
        Role afterRole = userRoleUpdateRequest.role();

        if (beforeRole != afterRole) {
            user.updateRole(afterRole);

            eventPublisher.publishEvent(new SessionDeletedRequestEvent(
                    userId,
                    SessionInvalidationReason.ROLE_CHANGED
            ));
            String title = "내 권한이 변경되었어요.";
            String content = "내 권한이 [%s]에서 [%s]로 변경되었어요.".formatted(beforeRole, afterRole);
            eventPublisher.publishEvent(new UserRoleUpdatedEvent(title, content, userId));
        }
        return userMapper.toDto(user);
    }

    private String resolveImageUrl(BinaryContent binaryContent) {
        if (binaryContent == null) return null;
        return binaryContentUrlResolver.resolve(binaryContent.getId());
    }
}
