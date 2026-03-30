package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserException;
import com.codeit.otboo.domain.user.fixture.UserCreateRequestFixture;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.fixture.UserResponseFixture;
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
            assertThatThrownBy(()->userService.createUser(userCreateRequest))
                    .isInstanceOf(UserException.class);

            // then
            then(userRepository).should().existsByEmail(userCreateRequest.email());
            then(passwordEncoder).should(never()).encode(any());
            then(userRepository).should(never()).save(any());
        }

    }
}