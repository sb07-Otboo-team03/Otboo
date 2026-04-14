package com.codeit.otboo.domain.user.mapper;

import com.codeit.otboo.domain.profile.dto.response.LocationResponse;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Location;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProfileMapper {

    public ProfileResponse toDto(User user, String profileImageUrl) {
        Profile profile = user.getProfile();

        return ProfileResponse.builder()
                .userId(user.getId())
                .name(profile.getName())
                .gender(profile.getGender())
                .birthDate(profile.getBirthDate())
                .location(toLocationResponse(profile.getLocation()))
                .temperatureSensitivity(profile.getTemperatureSensitivity())
                .profileImageUrl(profileImageUrl)
                .build();
    }

    private LocationResponse toLocationResponse(Location location) {
        if(location == null) return null;
        List<String> locationsMap = new ArrayList<>();
        locationsMap.add(location.getRegion1depthName());
        locationsMap.add(location.getRegion2depthName());
        locationsMap.add(location.getRegion3depthName());
        locationsMap.add(location.getRegion4depthName());

        return new LocationResponse(
                location.getLatitude(),
                location.getLongitude(),
                location.getX(),
                location.getY(),
                locationsMap
        );
    }
}