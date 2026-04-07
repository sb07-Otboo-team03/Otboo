package com.codeit.otboo.domain.weather.batch.dto;

import com.codeit.otboo.domain.notification.entity.Notification;

import java.util.List;

public record RegionAlertResult(
        Integer x,
        Integer y,
        List<Notification> notifications
) {
    public boolean isEmpty() {
        return notifications == null || notifications.isEmpty();
    }
}
