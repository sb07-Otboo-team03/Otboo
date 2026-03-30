package com.codeit.otboo.domain.notification.exception.notification;

import com.codeit.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends NotificationException {

    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND,
            HttpStatus.NOT_FOUND);
    }

    public NotificationNotFoundException(UUID notificationId) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND,
            Map.of("notificationId : ", notificationId.toString()),
            HttpStatus.NOT_FOUND);
    }
}
