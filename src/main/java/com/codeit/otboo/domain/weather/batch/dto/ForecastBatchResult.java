package com.codeit.otboo.domain.weather.batch.dto;

import com.codeit.otboo.domain.weather.entity.Weather;

import java.util.List;

public record ForecastBatchResult(
        int x,
        int y,
        List<Weather> weathers
) {
}
