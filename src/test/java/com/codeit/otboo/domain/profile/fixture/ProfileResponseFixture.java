package com.codeit.otboo.domain.profile.fixture;

import com.codeit.otboo.domain.profile.ProfileFixture;
import com.codeit.otboo.domain.profile.dto.request.ProfileUpdateRequest;
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

    public static ProfileResponse create(User user, String profileImageUrl, ProfileUpdateRequest request) {
        Profile profile = user.getProfile();
        LocationResponse locationResponse = LocationResponseFixture.create();
        return ProfileResponse.builder()
                .userId(user.getId())
                .name(request.name() != null ? request.name() : profile.getName())
                .gender(request.gender() != null ? request.gender() : profile.getGender())
                .birthDate(request.birthDate() != null ? request.birthDate() : profile.getBirthDate())
                .location(locationResponse)
                .temperatureSensitivity(request.temperatureSensitivity() != null ? request.temperatureSensitivity() : profile.getTemperatureSensitivity())
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
