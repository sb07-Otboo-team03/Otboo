package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.notification.dto.NotificationCreateCommand;
import com.codeit.otboo.domain.notification.dto.NotificationDto;

import java.util.List;
import java.util.UUID;

public interface NotificationEventService {
    NotificationDto createSingleNotification(UUID receiverId, String title, String content);

    List<NotificationDto> createMultipleNotifications(List<UUID> receiverIds, String title, String content);

    List<NotificationDto> createMultipleNotificationAllByReceivers(String title, String content);

    List<NotificationDto> createNotificationsFromCommands(List<NotificationCreateCommand> commands);
}
