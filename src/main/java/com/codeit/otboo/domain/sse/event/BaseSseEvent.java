package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.entity.Notification;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BaseSseEvent {
    private final List<Notification> notificationList;
}

