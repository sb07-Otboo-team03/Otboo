package com.codeit.otboo.domain.profile.dto.response;

import com.codeit.otboo.domain.profile.entity.Gender;
import com.codeit.otboo.domain.profile.entity.Location;

import java.util.UUID;

public record ProfileResponse(
    UUID userId,
    String name,
    Gender gender,
    Location location,
    int temperatureSensitivity,
    String profileImageUrl
) {
}
