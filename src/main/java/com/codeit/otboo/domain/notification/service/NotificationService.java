package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.global.security.OtbooUserDetails;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import java.util.UUID;

public interface NotificationService {

    CursorResponse<NotificationResponse> getNotifications(
        UUID userId, CursorRequest cursorRequest);

    void deleteNotification(UUID userId, UUID notificationId);
}
