package com.codeit.otboo.global.init;

import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.properties.AdminAccountProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminBootstrapService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAccountProperties adminProperties;

    @Transactional
    public void initialize() {
        User admin = userRepository.findByEmail(adminProperties.email())
                .orElseGet(this::createAdmin);

        if (!Role.ADMIN.equals(admin.getRole())) {
            admin.updateRole(Role.ADMIN);
        }

        if (admin.isLocked()) {
            admin.updateLockStatus(false);
        }

        Profile profile = admin.getProfile();
        if (profile == null) {
            profile = Profile.builder()
                    .user(admin)
                    .name(adminProperties.name())
                    .build();
            admin.setProfile(profile);
        } else if (!adminProperties.name().equals(profile.getName())) {
            profile.update(adminProperties.name(), null, null, null, null, null);
        }

        if (!passwordEncoder.matches(adminProperties.password(), admin.getPassword())) {
            admin.updatePassword(passwordEncoder.encode(adminProperties.password()));
        }
    }

    private User createAdmin() {
        User admin = User.builder()
                .email(adminProperties.email())
                .password(passwordEncoder.encode(adminProperties.password()))
                .build();

        admin.updateRole(Role.ADMIN);
        admin.updateLockStatus(false);

        Profile profile = Profile.builder()
                .user(admin)
                .name(adminProperties.name())
                .build();
        admin.setProfile(profile);

        return userRepository.save(admin);
    }
}