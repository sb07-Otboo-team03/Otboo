package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.dto.NotificationCreateCommand;

import java.util.List;

public record WeatherSseEvent (
        List<NotificationCreateCommand> notificationCommands
) {}
