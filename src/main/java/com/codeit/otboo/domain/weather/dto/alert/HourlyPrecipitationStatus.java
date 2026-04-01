package com.codeit.otboo.domain.weather.dto.alert;

import com.codeit.otboo.domain.weather.entity.PrecipitationType;

import java.time.LocalTime;

public record HourlyPrecipitationStatus(
        LocalTime time,
        PrecipitationType precipitationType
) {
}
