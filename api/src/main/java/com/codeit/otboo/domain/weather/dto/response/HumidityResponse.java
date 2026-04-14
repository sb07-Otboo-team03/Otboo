package com.codeit.otboo.domain.weather.dto.response;

public record HumidityResponse (
    Double current,
    Double comparedToDayBefore
) {}
