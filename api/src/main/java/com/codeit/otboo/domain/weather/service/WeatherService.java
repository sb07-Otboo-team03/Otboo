package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.dto.response.WeatherAPILocationResponse;
import com.codeit.otboo.domain.weather.dto.response.WeatherResponse;

import java.util.List;

public interface WeatherService {
    List<WeatherResponse> getAll(double longitude, double latitude);
    WeatherAPILocationResponse getLocation(double longitude, double latitude);
}
