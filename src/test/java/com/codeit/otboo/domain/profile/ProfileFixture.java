package com.codeit.otboo.domain.profile;

import com.codeit.otboo.domain.profile.entity.Location;
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

    public static Profile createWithName(User user, String name) {
        Profile profile = Profile.builder()
                .user(user)
                .name(name)
                .build();

        user.setProfile(profile);

        return profile;
    }
}
