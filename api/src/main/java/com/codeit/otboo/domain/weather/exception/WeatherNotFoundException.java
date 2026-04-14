package com.codeit.otboo.domain.weather.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class WeatherNotFoundException extends WeatherException {
    public WeatherNotFoundException(UUID weatherId) {
        super(
                ErrorCode.WEATHER_NOT_FOUND,
                Map.of("weatherId", weatherId.toString()),
                HttpStatus.NOT_FOUND
        );
    }

    public WeatherNotFoundException() {
        super(ErrorCode.WEATHER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
