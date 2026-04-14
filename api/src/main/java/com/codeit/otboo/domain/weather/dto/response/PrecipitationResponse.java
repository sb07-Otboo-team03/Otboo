package com.codeit.otboo.domain.weather.dto.response;

import com.codeit.otboo.domain.weather.entity.PrecipitationType;

public record PrecipitationResponse (
    PrecipitationType type,
    Double amount,
    Double probability
) {}
