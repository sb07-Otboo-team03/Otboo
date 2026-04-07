package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.entity.Notification;
import lombok.Getter;

import java.util.List;

@Getter
public class FeedCreatedEvent extends BaseSseEvent {
    public FeedCreatedEvent(List<Notification> notificationList) {
        super(notificationList);
    }
}