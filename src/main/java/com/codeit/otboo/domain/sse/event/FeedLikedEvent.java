package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.entity.Notification;
import lombok.Getter;

import java.util.List;

@Getter
public class FeedLikedEvent extends BaseSseEvent {
    public FeedLikedEvent(List<Notification> notificationList) {
        super(notificationList);
    }
}
