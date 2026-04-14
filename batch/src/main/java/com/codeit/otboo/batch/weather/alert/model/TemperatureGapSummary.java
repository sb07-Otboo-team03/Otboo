package com.codeit.otboo.batch.weather.alert.model;

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