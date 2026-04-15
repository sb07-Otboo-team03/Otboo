package com.codeit.otboo.domain.kafka.event;

import com.codeit.otboo.domain.notification.dto.NotificationDto;

import java.util.List;

public record NotificationBatchSseKafkaEvent(
        List<NotificationDto> notifications
) {
}