package com.codeit.otboo.domain.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KmaItems(
        List<KmaWeatherItem> item
) {}