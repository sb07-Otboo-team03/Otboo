package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.entity.Notification;
import java.util.List;
import lombok.Getter;

@Getter
public class FollowSseEvent extends BaseSseEvent {
    public FollowSseEvent(List<Notification> notificationList) {
        super(notificationList);
    }
}
