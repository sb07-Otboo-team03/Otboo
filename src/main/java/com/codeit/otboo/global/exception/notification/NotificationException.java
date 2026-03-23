package com.codeit.otboo.global.exception.notification;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class NotificationException extends OtbooException {

    public NotificationException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }

    public NotificationException(ErrorCode errorCode,
        Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }
}
