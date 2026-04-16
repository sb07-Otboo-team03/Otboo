package com.codeit.otboo.domain.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KmaResponse(
        KmaHeader header,
        KmaBody body
) {}
