package com.codeit.otboo.batch.weather.alert.model;

import java.time.LocalTime;

public record HourlyTemperature(
        LocalTime time,
        double temperature
) {
}
