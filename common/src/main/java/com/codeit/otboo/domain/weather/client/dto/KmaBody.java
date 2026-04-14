package com.codeit.otboo.domain.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KmaBody(
        String dataType,
        KmaItems items,
        int pageNo,
        int numOfRows,
        int totalCount
) {}
