package com.codeit.otboo.domain.weather.dto.response;

import com.codeit.otboo.domain.weather.entity.WindAsWord;

public record WindSpeedResponse (
    Double speed,
    WindAsWord asWord
){}
