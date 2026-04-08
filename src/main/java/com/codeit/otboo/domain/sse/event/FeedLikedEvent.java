package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FeedLikedEvent {
    List<Notification> notificationList;
}
