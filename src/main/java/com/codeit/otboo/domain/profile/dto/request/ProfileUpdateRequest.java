package com.codeit.otboo.domain.profile.dto.request;

import com.codeit.otboo.domain.profile.entity.Gender;
import com.codeit.otboo.domain.profile.entity.Location;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        @Size(min = 2, max = 20)
        String name,
        Gender gender,
        LocalDate birthDate,
        LocationRequest location,
        @Min(1) @Max(5)
        int temperatureSensitivity
) {
}
