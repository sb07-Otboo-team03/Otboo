package com.codeit.otboo.batch.weather.alert.model;

import com.codeit.otboo.domain.notification.dto.NotificationCreateCommand;

import java.util.List;

public record RegionAlertResult(
        Integer x,
        Integer y,
        List<NotificationCreateCommand> commands
) {
    public boolean isEmpty() {
        return commands == null || commands.isEmpty();
    }
}
