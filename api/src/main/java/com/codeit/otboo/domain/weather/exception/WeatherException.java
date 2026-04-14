package com.codeit.otboo.domain.weather.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class WeatherException extends OtbooException {
    public WeatherException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }

    public WeatherException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
