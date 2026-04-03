package com.codeit.otboo.domain.profile.fixture;

import com.codeit.otboo.domain.profile.ProfileFixture;
import com.codeit.otboo.domain.profile.dto.response.LocationResponse;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;

public class ProfileResponseFixture {
    public static ProfileResponse create(User user, String profileImageUrl) {
        Profile profile = user.getProfile();
        LocationResponse locationResponse = LocationResponseFixture.create();
        return ProfileResponse.builder()
                .userId(user.getId())
                .name(profile.getName())
                .gender(profile.getGender())
                .birthDate(profile.getBirthDate())
                .location(locationResponse)
                .temperatureSensitivity(profile.getTemperatureSensitivity())
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
