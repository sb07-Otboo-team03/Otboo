package com.codeit.otboo.domain.weather.dto.response;

import com.codeit.otboo.domain.weather.entity.SkyStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WeatherResponse (
    UUID id,
    LocalDateTime forecastedAt,
    LocalDateTime forecastAt,
    WeatherAPILocationResponse location,
    SkyStatus skyStatus,
    PrecipitationResponse precipitation,
    HumidityResponse humidity,
    TemperatureResponse temperature,
    WindSpeedResponse windSpeed
) {}
