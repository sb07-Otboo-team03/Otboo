package com.codeit.otboo.domain.weather.dto.response;

import java.util.List;

public record WeatherAPILocationResponse (
        Double latitude,
        Double longitude,
        Integer x,
        Integer y,
        List<String> locationNames // 지명 입력
){}
