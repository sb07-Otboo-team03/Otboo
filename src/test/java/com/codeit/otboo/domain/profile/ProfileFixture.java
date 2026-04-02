package com.codeit.otboo.domain.profile;

import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;

public class ProfileFixture {
    public static Profile create(User user) {
        Profile profile = Profile.builder()
                .user(user)
                .name("아무이름")
                .build();

        user.setProfile(profile);

        return profile;
    }
}
