package com.codeit.otboo.domain.profile.dto.response;

import com.codeit.otboo.domain.profile.entity.Gender;
import com.codeit.otboo.domain.profile.entity.Location;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponse(
    UUID userId,
    String name,
    Gender gender,
    LocalDate birthDate,
    LocationResponse location,
    int temperatureSensitivity,
    String profileImageUrl
) {
}
