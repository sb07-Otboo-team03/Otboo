package com.codeit.otboo.domain.weather.dto.response;

import com.codeit.otboo.domain.weather.entity.SkyStatus;

import java.util.UUID;

public class WeatherSummaryDto {
    private UUID weatherId;
    private SkyStatus skyStatus;
    private PrecipitationDto precipitation;
    private TemperatureDto temperature;
}
