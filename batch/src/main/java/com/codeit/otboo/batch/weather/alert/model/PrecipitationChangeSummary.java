package com.codeit.otboo.batch.weather.alert.model;

import com.codeit.otboo.domain.weather.entity.PrecipitationType;

import java.time.LocalTime;

public record PrecipitationChangeSummary(
        LocalTime startTime,
        PrecipitationType startType,
        LocalTime endTime,
        PrecipitationType endType,
        boolean shouldNotify,
        String content
) {
    public static PrecipitationChangeSummary empty() {
        return new PrecipitationChangeSummary(null, null, null, null, false, null);
    }
}
