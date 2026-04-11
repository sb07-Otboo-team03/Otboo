package com.codeit.otboo.batch.weather.alert.model;

import java.util.UUID;

public record AlertTarget(
        UUID userId,
        Integer x,
        Integer y
) {}
