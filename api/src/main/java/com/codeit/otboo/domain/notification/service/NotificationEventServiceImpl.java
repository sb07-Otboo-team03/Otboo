package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.notification.dto.NotificationCreateCommand;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationEventServiceImpl implements NotificationEventService{ // 이벤트 기반 알림 생성 서비스
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationDto createSingleNotification(UUID userId, String title, String content) {
        NotificationCreateCommand notificationCreateCommand = new NotificationCreateCommand(
                userId,
                title,
                content,
                NotificationLevel.INFO
        );
        Notification notification = notificationService.create(notificationCreateCommand);
        return notificationMapper.toDto(notification);
    }
}
