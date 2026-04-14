package com.codeit.otboo.domain.weather.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class YesterdayWeatherNotFoundException extends WeatherException {
    public YesterdayWeatherNotFoundException(int x, int y, LocalDate date, LocalTime time) {
        super(
                ErrorCode.YESTERDAY_WEATHER_NOT_FOUND,
                Map.of("x", String.valueOf(x),
                        "y", String.valueOf(y),
                        "date", date.toString(),
                        "hour", time.toString()),
                HttpStatus.NOT_FOUND
        );
    }

    public YesterdayWeatherNotFoundException() {
        super(ErrorCode.YESTERDAY_WEATHER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
