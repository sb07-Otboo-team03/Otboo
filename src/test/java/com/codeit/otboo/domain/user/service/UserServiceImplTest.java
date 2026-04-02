package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.profile.ProfileFixture;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.profile.fixture.ProfileResponseFixture;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserException;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.UserCreateRequestFixture;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.fixture.UserResponseFixture;
import com.codeit.otboo.domain.user.mapper.ProfileMapper;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private BinaryContentUrlResolver binaryContentUrlResolver;

    @InjectMocks
    private UserServiceImpl userService;


    @Nested
    @DisplayName("유저 생성")
    class UserCreate {

        @DisplayName("정상 회원가입")
        @Test
        void createUser_success() {
            // given
            UserCreateRequest userCreateRequest = UserCreateRequestFixture.create();
            UUID userId = UUID.randomUUID();
            String encodedPassword = "encodedPassword";

            User savedUser = UserFixture.create(userId, userCreateRequest.email(), encodedPassword);
            UserResponse userResponse = UserResponseFixture.create(savedUser);

            given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(userMapper.toDto(any(User.class))).willReturn(userResponse);

            // when
            UserResponse result = userService.createUser(userCreateRequest);

            // then
            assertThat(result).isEqualTo(userResponse);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().existsByEmail(userCreateRequest.email());
            then(passwordEncoder).should().encode(userCreateRequest.password());
            then(userRepository).should().save(userCaptor.capture());
            then(userMapper).should().toDto(savedUser);

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo(userCreateRequest.email());
            assertThat(capturedUser.getPassword()).isEqualTo(encodedPassword);
            assertThat(capturedUser.getProfile()).isNotNull();
            assertThat(capturedUser.getProfile().getName()).isEqualTo(userCreateRequest.name());
        }

        @DisplayName("회원 가입 실패 - 중복된 이메일")
        @Test
        void createUser_fail() {
            // given
            UserCreateRequest userCreateRequest = UserCreateRequestFixture.create();
            given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(true);

            // when
            assertThatThrownBy(() -> userService.createUser(userCreateRequest))
                    .isInstanceOf(UserException.class);

            // then
            then(userRepository).should().existsByEmail(userCreateRequest.email());
            then(passwordEncoder).should(never()).encode(any());
            then(userRepository).should(never()).save(any());
        }

    }

    @Nested
    @DisplayName("프로필 조회")
    class GetProfile {
            @Test
            @DisplayName("프로필 조회 성공")
            void getProfile_success() {
                // given
                UUID userId = UUID.randomUUID();
                UUID binaryContentId = UUID.randomUUID();

                User user = UserFixture.create(userId, "test@codeit.com", "password123!");
                Profile profile = ProfileFixture.create(user);
                BinaryContent binaryContent = BinaryContentFixture.create(binaryContentId);

                user.setProfile(profile);
                profile.update(null, null, null, null, null, binaryContent);

                ProfileResponse profileResponse = ProfileResponseFixture.create(user, "test-image-url");

                given(userRepository.findById(userId)).willReturn(Optional.of(user));
                given(binaryContentUrlResolver.resolve(binaryContentId)).willReturn("test-image-url");
                given(profileMapper.toDto(user, "test-image-url")).willReturn(profileResponse);

                // when
                ProfileResponse result = userService.getProfile(userId);

                // then
                assertThat(result).isEqualTo(profileResponse);
                then(userRepository).should().findById(userId);
                then(binaryContentUrlResolver).should().resolve(binaryContentId);
                then(profileMapper).should().toDto(user, "test-image-url");
            }

            @Test
            @DisplayName("프로필 조회 성공 - 이미지 없는 경우")
            void getProfile_success_nonProfile() {
                // given
                UUID userId = UUID.randomUUID();
                User user = UserFixture.create(userId, "test@codeit.com", "password123!");
                Profile profile = ProfileFixture.create(user);
                user.setProfile(profile);
                profile.update(null, null, null, null, null, null);
                ProfileResponse profileResponse = ProfileResponseFixture.create(user, null);

                given(userRepository.findById(userId)).willReturn(Optional.of(user));
                given(profileMapper.toDto(user, null)).willReturn(profileResponse);

                // when
                ProfileResponse result = userService.getProfile(userId);

                // then
                assertThat(result).isEqualTo(profileResponse);
                then(binaryContentUrlResolver).should(never()).resolve(any());
                then(profileMapper).should().toDto(user, null);
            }


            @Test
            @DisplayName("프로필 조회 실패 - 존재하지 않는 유저 ")
            void getProfile_fail_userNotFound() {
                // given
                UUID userId = UUID.randomUUID();

                given(userRepository.findById(userId)).willReturn(Optional.empty());

                // when
                assertThatThrownBy(() -> userService.getProfile(userId))
                        .isInstanceOf(UserNotFoundException.class);

                // then
                then(binaryContentUrlResolver).shouldHaveNoInteractions();
                then(profileMapper).shouldHaveNoInteractions();
            }
        }
    }