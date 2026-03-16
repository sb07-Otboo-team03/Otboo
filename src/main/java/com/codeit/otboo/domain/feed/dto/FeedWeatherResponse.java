package com.codeit.otboo.domain.feed.dto;

import com.codeit.otboo.domain.weather.dto.response.PrecipitationResponse;
import com.codeit.otboo.domain.weather.dto.response.TemperatureResponse;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FeedWeatherResponse(
    UUID weatherId,
    SkyStatus skyStatus,
    PrecipitationResponse precipitation,
    TemperatureResponse temperature
) {}
