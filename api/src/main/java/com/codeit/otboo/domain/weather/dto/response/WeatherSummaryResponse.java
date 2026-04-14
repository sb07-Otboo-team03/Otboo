package com.codeit.otboo.domain.weather.dto.response;

import com.codeit.otboo.domain.weather.entity.SkyStatus;

import java.util.UUID;

public record WeatherSummaryResponse (
    UUID weatherId,
    SkyStatus skyStatus,
    PrecipitationResponse precipitation,
    TemperatureResponse temperature
){}
