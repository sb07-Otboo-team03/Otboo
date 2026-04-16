package com.codeit.otboo.global.init;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.codeit.otboo.domain.profile.ProfileFixture;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.properties.AdminAccountProperties;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminAccountProperties adminProperties;

    @InjectMocks
    private AdminBootstrapService adminBootstrapService;

    @Nested
    @DisplayName("관리자 초기화")
    class Initialize {

        @Test
        @DisplayName("관리자 초기화 성공 - 기존 관리자 없으면 새로 생성")
        void initialize_success_createAdmin_whenAdminNotExists() {
            // given
            String email = "system@otboo.io";
            String name = "System";
            String password = "otboo1!";
            String encodedPassword = "encoded-password";

            given(adminProperties.email()).willReturn(email);
            given(adminProperties.name()).willReturn(name);
            given(adminProperties.password()).willReturn(password);

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            given(passwordEncoder.encode(password)).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            adminBootstrapService.initialize();

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(userCaptor.capture());

            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getEmail()).isEqualTo(email);
            assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
            assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
            assertThat(savedUser.isLocked()).isFalse();

            assertThat(savedUser.getProfile()).isNotNull();
            assertThat(savedUser.getProfile().getName()).isEqualTo(name);
            assertThat(savedUser.getProfile().getUser()).isEqualTo(savedUser);
        }

        @Test
        @DisplayName("관리자 초기화 성공 - 기존 유저 권한이 ADMIN이 아니면 ADMIN으로 변경")
        void initialize_success_updateRole_whenRoleIsNotAdmin() {
            // given
            String email = "system@otboo.io";
            String name = "System";
            String password = "otboo1!";

            User user = UserFixture.create(UUID.randomUUID(), email, "encoded-password");
            user.updateRole(Role.USER);
            user.updateLockStatus(false);

            Profile profile = ProfileFixture.create(user);
            profile.update(name, null, null, null, null, null);
            user.setProfile(profile);

            given(adminProperties.email()).willReturn(email);
            given(adminProperties.name()).willReturn(name);
            given(adminProperties.password()).willReturn(password);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

            // when
            adminBootstrapService.initialize();

            // then
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);

            then(userRepository).should().findByEmail(email);
            then(userRepository).should(never()).save(any());
            then(passwordEncoder).should().matches(password, user.getPassword());
            then(passwordEncoder).should(never()).encode(any());
        }

        @Test
        @DisplayName("관리자 초기화 성공 - 잠긴 계정이면 잠금 해제")
        void initialize_success_unlock_whenAdminLocked() {
            // given
            String email = "system@otboo.io";
            String name = "System";
            String password = "otboo1!";

            User user = UserFixture.create(UUID.randomUUID(), email, "encoded-password");
            user.updateRole(Role.ADMIN);
            user.updateLockStatus(true);

            Profile profile = ProfileFixture.create(user);
            profile.update(name, null, null, null, null, null);
            user.setProfile(profile);

            given(adminProperties.email()).willReturn(email);
            given(adminProperties.name()).willReturn(name);
            given(adminProperties.password()).willReturn(password);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

            // when
            adminBootstrapService.initialize();

            // then
            assertThat(user.isLocked()).isFalse();

            then(userRepository).should().findByEmail(email);
            then(userRepository).should(never()).save(any());
            then(passwordEncoder).should().matches(password, user.getPassword());
            then(passwordEncoder).should(never()).encode(any());
        }

        @Test
        @DisplayName("관리자 초기화 성공 - 프로필이 없으면 생성")
        void initialize_success_createProfile_whenProfileIsNull() {
            // given
            String email = "system@otboo.io";
            String name = "System";
            String password = "otboo1!";

            User user = UserFixture.create(UUID.randomUUID(), email, "encoded-password");
            user.updateRole(Role.ADMIN);
            user.updateLockStatus(false);
            user.setProfile(null);

            given(adminProperties.email()).willReturn(email);
            given(adminProperties.name()).willReturn(name);
            given(adminProperties.password()).willReturn(password);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

            // when
            adminBootstrapService.initialize();

            // then
            assertThat(user.getProfile()).isNotNull();
            assertThat(user.getProfile().getName()).isEqualTo(name);
            assertThat(user.getProfile().getUser()).isEqualTo(user);

            then(userRepository).should().findByEmail(email);
            then(userRepository).should(never()).save(any());
            then(passwordEncoder).should().matches(password, user.getPassword());
            then(passwordEncoder).should(never()).encode(any());
        }

        @Test
        @DisplayName("관리자 초기화 성공 - 프로필 이름이 다르면 변경")
        void initialize_success_updateProfileName_whenNameDifferent() {
            // given
            String email = "system@otboo.io";
            String name = "System";
            String password = "otboo1!";

            User user = UserFixture.create(UUID.randomUUID(), email, "encoded-password");
            user.updateRole(Role.ADMIN);
            user.updateLockStatus(false);

            Profile profile = ProfileFixture.create(user);
            profile.update("기존 이름", null, null, null, null, null);
            user.setProfile(profile);

            given(adminProperties.email()).willReturn(email);
            given(adminProperties.name()).willReturn(name);
            given(adminProperties.password()).willReturn(password);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

            // when
            adminBootstrapService.initialize();

            // then
            assertThat(user.getProfile().getName()).isEqualTo(name);

            then(userRepository).should().findByEmail(email);
            then(userRepository).should(never()).save(any());
            then(passwordEncoder).should().matches(password, user.getPassword());
            then(passwordEncoder).should(never()).encode(any());
        }

        @Test
        @DisplayName("관리자 초기화 성공 - 비밀번호가 다르면 변경")
        void initialize_success_updatePassword_whenPasswordDifferent() {
            // given
            String email = "system@otboo.io";
            String name = "System";
            String password = "otboo1!";
            String newEncodedPassword = "new-encoded-password";

            User user = UserFixture.create(UUID.randomUUID(), email, "old-encoded-password");
            user.updateRole(Role.ADMIN);
            user.updateLockStatus(false);

            Profile profile = ProfileFixture.create(user);
            profile.update(name, null, null, null, null, null);
            user.setProfile(profile);

            given(adminProperties.email()).willReturn(email);
            given(adminProperties.name()).willReturn(name);
            given(adminProperties.password()).willReturn(password);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, user.getPassword())).willReturn(false);
            given(passwordEncoder.encode(password)).willReturn(newEncodedPassword);

            // when
            adminBootstrapService.initialize();

            // then
            assertThat(user.getPassword()).isEqualTo(newEncodedPassword);

            then(userRepository).should().findByEmail(email);
            then(userRepository).should(never()).save(any());
            then(passwordEncoder).should().matches(password, "old-encoded-password");
            then(passwordEncoder).should().encode(password);
        }

        @Test
        @DisplayName("관리자 초기화 성공 - 비밀번호가 같으면 변경하지 않음")
        void initialize_success_doNotUpdatePassword_whenPasswordSame() {
            // given
            String email = "system@otboo.io";
            String name = "System";
            String password = "otboo1!";

            User user = UserFixture.create(UUID.randomUUID(), email, "encoded-password");
            user.updateRole(Role.ADMIN);
            user.updateLockStatus(false);

            Profile profile = ProfileFixture.create(user);
            profile.update(name, null, null, null, null, null);
            user.setProfile(profile);

            given(adminProperties.email()).willReturn(email);
            given(adminProperties.name()).willReturn(name);
            given(adminProperties.password()).willReturn(password);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

            // when
            adminBootstrapService.initialize();

            // then
            assertThat(user.getPassword()).isEqualTo("encoded-password");

            then(userRepository).should().findByEmail(email);
            then(userRepository).should(never()).save(any());
            then(passwordEncoder).should().matches(password, user.getPassword());
            then(passwordEncoder).should(never()).encode(any());
        }

        @Test
        @DisplayName("관리자 초기화 성공 - 이미 정상 상태면 save 없이 유지")
        void initialize_success_keepAdmin_whenAlreadyNormalized() {
            // given
            String email = "system@otboo.io";
            String name = "System";
            String password = "otboo1!";

            User user = UserFixture.create(UUID.randomUUID(), email, "encoded-password");
            user.updateRole(Role.ADMIN);
            user.updateLockStatus(false);

            Profile profile = ProfileFixture.create(user);
            profile.update(name, null, null, null, null, null);
            user.setProfile(profile);

            given(adminProperties.email()).willReturn(email);
            given(adminProperties.name()).willReturn(name);
            given(adminProperties.password()).willReturn(password);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

            // when
            adminBootstrapService.initialize();

            // then
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
            assertThat(user.isLocked()).isFalse();
            assertThat(user.getProfile().getName()).isEqualTo(name);
            assertThat(user.getPassword()).isEqualTo("encoded-password");

            then(userRepository).should().findByEmail(email);
            then(userRepository).should(never()).save(any());
            then(passwordEncoder).should().matches(password, user.getPassword());
            then(passwordEncoder).should(never()).encode(any());
        }
    }
}