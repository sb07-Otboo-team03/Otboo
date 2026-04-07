package com.codeit.otboo.domain.sse.event;

import com.codeit.otboo.domain.notification.entity.Notification;

import java.util.List;

public class WeatherSseEvent extends BaseSseEvent{
    public WeatherSseEvent(List<Notification> notificationList) { super(notificationList);}
}
