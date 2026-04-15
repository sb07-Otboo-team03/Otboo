package com.codeit.otboo.domain.kafka.event;

import com.codeit.otboo.domain.notification.dto.NotificationDto;

public record NotificationSseKafkaEvent(
        NotificationDto notification
) {
}