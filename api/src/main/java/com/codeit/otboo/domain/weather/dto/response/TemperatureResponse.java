package com.codeit.otboo.domain.weather.dto.response;

public record TemperatureResponse (
    Double current,
    Double comparedToDayBefore,
    Double min,
    Double max
){}