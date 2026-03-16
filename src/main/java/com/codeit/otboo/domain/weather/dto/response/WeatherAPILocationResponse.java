package com.codeit.otboo.domain.weather.dto.response;

import java.util.ArrayList;

public record WeatherAPILocationResponse (
    Double latitude,
    Double longitude,
    Integer x,
    Integer y,
    ArrayList<String> locationNames // 지명 입력
){}
