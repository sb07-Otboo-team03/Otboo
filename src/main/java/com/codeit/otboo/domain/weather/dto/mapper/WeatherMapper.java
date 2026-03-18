package com.codeit.otboo.domain.weather.dto.mapper;

import com.codeit.otboo.domain.feed.entity.FeedWeather;
import com.codeit.otboo.domain.weather.dto.response.PrecipitationResponse;
import com.codeit.otboo.domain.weather.dto.response.TemperatureResponse;
import com.codeit.otboo.domain.weather.dto.response.WeatherSummaryResponse;

public class WeatherMapper {

    public static WeatherSummaryResponse toSummaryDto(FeedWeather weather) {
        PrecipitationResponse precipitationResponse = new PrecipitationResponse(
                weather.getPrecipitationType(),
                weather.getPrecipitationAmount(),
                weather.getPrecipitationProbability()
        );

        TemperatureResponse temperatureResponse = new TemperatureResponse(
                weather.getTemperatureCurrent(),
                weather.getTemperatureComparedToDayBefore(),
                weather.getTemperatureMin(),
                weather.getTemperatureMax()
        );

        return new WeatherSummaryResponse(
                weather.getWeatherId(),
                weather.getSkyStatus(),
                precipitationResponse,
                temperatureResponse
        );
    }
}
