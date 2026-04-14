package com.codeit.otboo.batch.weather.alert.model;

import com.codeit.otboo.domain.weather.entity.PrecipitationType;

import java.time.LocalTime;

public record HourlyPrecipitationStatus(
        LocalTime time,
        PrecipitationType precipitationType
) {
}
