package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.entity.Notification;
import java.util.List;

public class DirectMessageSseEvent extends BaseSseEvent {
    public DirectMessageSseEvent(List<Notification> notificationList) {
        super(notificationList);
    }
}
