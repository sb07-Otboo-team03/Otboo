package com.codeit.otboo.domain.weather.dto.response;

import com.codeit.otboo.domain.weather.entity.SkyStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class WeatherDto {
    private UUID id;
    private LocalDateTime forecastedAt;
    private LocalDateTime forecastAt;
    private WeatherAPILocation location;
    private SkyStatus skyStatus;
    private PrecipitationDto precipitationStatus;
    private HumidityDto humidity;
    private TemperatureDto temperatureStatus;
    private WindSpeedDto windSpeed;
}
