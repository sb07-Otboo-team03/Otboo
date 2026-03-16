package com.codeit.otboo.domain.profile.dto.request;

import com.codeit.otboo.domain.profile.entity.Gender;
import com.codeit.otboo.domain.profile.entity.Location;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        String name,
        Gender gender,
        LocalDate birthDate,
        LocationRequest location,
        int temperatureSensitivity
) {
}
