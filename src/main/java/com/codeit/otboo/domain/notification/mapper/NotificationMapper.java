package com.codeit.otboo.domain.notification.mapper;

import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toDto(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getCreatedAt(),
            notification.getReceiver().getId(),
            notification.getTitle(),
            notification.getContent(),
            notification.getLevel()
        );
    }
}
