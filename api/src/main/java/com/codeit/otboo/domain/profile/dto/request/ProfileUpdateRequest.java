package com.codeit.otboo.domain.profile.dto.request;

import com.codeit.otboo.domain.profile.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileUpdateRequest(
        @Pattern(regexp = "^(?!\\s*$).+", message = "공백은 허용되지 않습니다.")
        @Size(min = 2, max = 20)
        String name,
        Gender gender,
        LocalDate birthDate,
        LocationRequest location,
        @Min(1)
        @Max(5)
        Integer temperatureSensitivity,
        UUID binaryContentId
) {
}
