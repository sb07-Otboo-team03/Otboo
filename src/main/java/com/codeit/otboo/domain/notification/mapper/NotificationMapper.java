package com.codeit.otboo.domain.notification.mapper;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    public NotificationResponse toDto(NotificationDto notification) {
        return new NotificationResponse(
            notification.id(),
            notification.createdAt(),
            notification.receiverId(),
            notification.title(),
            notification.content(),
            notification.level()
        );
    }

    public NotificationDto toEventDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .createdAt(notification.getCreatedAt())
                .receiverId(notification.getReceiver().getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .level(notification.getLevel())
                .build();
    }
}
