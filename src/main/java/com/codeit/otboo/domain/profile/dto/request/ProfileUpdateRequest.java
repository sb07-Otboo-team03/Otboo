package com.codeit.otboo.domain.profile.dto.request;

import com.codeit.otboo.domain.profile.entity.Location;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        String name,
        String gender,
        LocalDate birthDate,
        Location location,
        int temperatureSensitivity
) {
}
