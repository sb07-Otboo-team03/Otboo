package com.codeit.otboo.domain.weather.controller;

import com.codeit.otboo.domain.weather.dto.response.WeatherAPILocationResponse;
import com.codeit.otboo.domain.weather.dto.response.WeatherResponse;
import com.codeit.otboo.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weathers")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<List<WeatherResponse>> getWeather(
            @RequestParam double longitude,
            @RequestParam double latitude
    ) {
        List<WeatherResponse> response = weatherService.getAll(longitude, latitude);
        return ResponseEntity.ok(response);
    }

    // 사용자 프로필 위치 설정시 호출
    @GetMapping("/location")
    public ResponseEntity<WeatherAPILocationResponse> getWeatherLocation(
            @RequestParam double longitude,
            @RequestParam double latitude
    ) {
        WeatherAPILocationResponse response = weatherService.getLocation(longitude, latitude);
        return ResponseEntity.ok(response);
    }

}
