package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import java.util.List;
import lombok.Builder;

@Builder
public record SseEvent(
    List<NotificationDto> notificationDtoList
) { }