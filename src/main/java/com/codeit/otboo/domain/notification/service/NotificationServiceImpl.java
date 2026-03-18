package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.global.slice.dto.PageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService{
    private final NotificationRepository notificationRepository;

    @Override
    public PageResponse<NotificationResponse> getNotifications(CursorRequest cursorRequest) {

        throw new UnsupportedOperationException("🚨for Test");
    }

    @Override
    public void deleteNotification(UUID notificationId) {

        throw new UnsupportedOperationException("🚨for Test");
    }
}
