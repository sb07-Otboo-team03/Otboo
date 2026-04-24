package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.notification.dto.NotificationCreateCommand;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationEventServiceImpl implements NotificationEventService { // 이벤트 기반 알림 생성 서비스
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    // 단일 유저 간 알림 처리
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

    // 다수의 유저 알림 처리
    @Override
    @Transactional
    public List<NotificationDto> createMultipleNotifications(List<UUID> userIds, String title, String content) {
        return userIds.stream()
                .map(userId -> createSingleNotification(userId, title, content))
                .toList();
    }

    // 전체 유저 알림 처리
    @Override
    @Transactional
    public List<NotificationDto> createMultipleNotificationAllByReceivers(String title, String content, String type) {
        return userService.getAllUsers().stream()
                .map(user -> createSingleNotification(user.getId(), title, content))
                .toList();
    }

    // 배치 커맨드용
    @Override
    @Transactional
    public List<NotificationDto> createNotificationsFromCommands(
            List<NotificationCreateCommand> commands
    ) {
        return commands.stream()
                .map(notificationService::create)
                .map(notificationMapper::toDto)
                .toList();
    }
}
