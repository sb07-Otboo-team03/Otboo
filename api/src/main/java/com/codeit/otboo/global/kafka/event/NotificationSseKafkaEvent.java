package com.codeit.otboo.global.kafka.event;

import com.codeit.otboo.domain.notification.dto.NotificationDto;

public record NotificationSseKafkaEvent(
        NotificationDto notification
) {
}