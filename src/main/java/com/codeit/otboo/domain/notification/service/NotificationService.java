package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.global.slice.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;

public interface NotificationService {

    PageResponse<NotificationResponse> getNotifications(CursorRequest cursorRequest);

    void deleteNotification(UUID notificationId);
}
