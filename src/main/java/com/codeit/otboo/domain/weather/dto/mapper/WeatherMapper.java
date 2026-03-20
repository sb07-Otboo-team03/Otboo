package com.codeit.otboo.domain.weather.dto.mapper;

import com.codeit.otboo.domain.feed.entity.FeedWeather;
import com.codeit.otboo.domain.weather.dto.response.*;
import com.codeit.otboo.domain.weather.entity.LocationNameMap;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeatherMapper {

    public WeatherSummaryResponse toSummaryDto(FeedWeather weather) {
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

    public List<WeatherResponse> toDto (List<Weather> weathers, LocationNameMap locationNameMap, YesterdayHourlyWeather yesterdayHourlyWeather) {
        WeatherAPILocationResponse location = new WeatherAPILocationResponse(
                locationNameMap.getLatitude(),
                locationNameMap.getLongitude(),
                locationNameMap.getX(),
                locationNameMap.getY(),
                List.of(locationNameMap.getRegion1depthName(),
                        locationNameMap.getRegion2depthName(),
                        locationNameMap.getRegion3depthName(),
                        locationNameMap.getRegion4depthName())
        );

        List<WeatherResponse> weatherResponseList = new ArrayList<>();

        for (int i = 0; i < weathers.size(); i++) {
            Weather w = weathers.get(i);

            PrecipitationResponse precipitationStatus = new PrecipitationResponse(
                    w.getPrecipitationType(),
                    w.getPrecipitationAmount(),
                    w.getPrecipitationProbability()
            );

            WindSpeedResponse windSpeed = new WindSpeedResponse(
                    w.getWindSpeed(),
                    w.getWindAsWord()
            );

            Double yesterdayTemperature;
            Double yesterdayHumidity;

            if (i == 0) { // 오늘자 데이터의 경우
                yesterdayTemperature = yesterdayHourlyWeather.getTemperature();
                yesterdayHumidity = yesterdayHourlyWeather.getHumidity();
            } else { // 내일 이후 데이터들은 이전 데이터와 비교
                yesterdayTemperature = weathers.get(i - 1).getTemperatureCurrent();
                yesterdayHumidity = weathers.get(i - 1).getHumidityCurrent();
            }

            HumidityResponse humidity = new HumidityResponse(
                    w.getHumidityCurrent(),
                    w.getHumidityCurrent() - yesterdayHumidity
            );

            TemperatureResponse temperatureStatus = new TemperatureResponse(
                    w.getHumidityCurrent(),
                    w.getTemperatureCurrent() - yesterdayTemperature,
                    w.getTemperatureMin(),
                    w.getTemperatureMax()
            );

            WeatherResponse weatherResponse = new WeatherResponse(
                    w.getId(),
                    w.getForecastedAt(),
                    w.getForecastAt(),
                    location,
                    w.getSkyStatus(),
                    precipitationStatus,
                    humidity,
                    temperatureStatus,
                    windSpeed
            );

            weatherResponseList.add(weatherResponse);
        }

        return weatherResponseList;
    }
}
