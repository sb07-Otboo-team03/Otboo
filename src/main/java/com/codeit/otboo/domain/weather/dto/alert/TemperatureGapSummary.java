package com.codeit.otboo.domain.weather.dto.alert;

import java.time.LocalTime;

public record TemperatureGapSummary(
        double averageGap,
        int maxAbsGap,
        int maxGap,
        LocalTime maxGapTime,
        double eveningAverageGap,
        boolean shouldNotify,
        String content
) {
}