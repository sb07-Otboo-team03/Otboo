package com.codeit.otboo.domain.notification.exception.notification;

import com.codeit.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class DuplicateNotificationException extends NotificationException {

    public DuplicateNotificationException() {
        super(ErrorCode.DUPLICATE_NOTIFICATION,
            HttpStatus.CONFLICT);
    }

    public DuplicateNotificationException(UUID notificationId) {
        super(ErrorCode.DUPLICATE_NOTIFICATION,
            Map.of("notificationId : ", notificationId.toString()),
            HttpStatus.CONFLICT);
    }
}
