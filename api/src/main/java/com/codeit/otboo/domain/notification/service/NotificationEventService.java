package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;

import java.util.UUID;

public interface NotificationEventService {
    NotificationDto createSingleNotification(UUID userId, String title, String content);
}
