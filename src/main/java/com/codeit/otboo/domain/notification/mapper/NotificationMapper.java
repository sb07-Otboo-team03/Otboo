package com.codeit.otboo.domain.notification.mapper;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toDto(NotificationDto notification) {
        return new NotificationResponse(
            notification.id(),
            notification.createdAt(),
            notification.receiver().getId(),
            notification.title(),
            notification.content(),
            notification.level()
        );
    }
}
