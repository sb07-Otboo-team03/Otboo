package com.codeit.otboo.batch.weather.alert.model;

import java.util.List;
import java.util.UUID;

public record RegionAlertTarget(
        Integer x,
        Integer y,
        List<UUID> userIds
) {
}
