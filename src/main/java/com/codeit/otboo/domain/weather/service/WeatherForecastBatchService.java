package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.batch.dto.ForecastBatchResult;
import com.codeit.otboo.domain.weather.client.KmaWeatherClient;
import com.codeit.otboo.domain.weather.client.KmaWeatherMapper;
import com.codeit.otboo.domain.weather.client.dto.KmaWeatherItem;
import com.codeit.otboo.domain.weather.entity.Weather;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherForecastBatchService {

    private static final int WEATHER_FORECAST_NUM_OF_ROWS = 1052;

    private final KmaWeatherClient kmaWeatherClient;
    private final KmaWeatherMapper kmaWeatherMapper;

    public ForecastBatchResult collect(int x, int y, String baseDate, String baseTime) {
        List<KmaWeatherItem> items = kmaWeatherClient.callWeatherApi(
                baseDate,
                baseTime,
                x,
                y,
                WEATHER_FORECAST_NUM_OF_ROWS
        );

        List<Weather> weathers = kmaWeatherMapper.toWeathers(
                baseTime,
                x,
                y,
                items,
                true
        );

        return new ForecastBatchResult(x, y, weathers);
    }
}
