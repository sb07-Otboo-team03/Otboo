package com.codeit.otboo.domain.weather.dto.alert;

import java.time.LocalTime;

public record HourlyTemperature(
        LocalTime time,
        double temperature
) {
}
