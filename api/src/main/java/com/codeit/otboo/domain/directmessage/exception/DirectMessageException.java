package com.codeit.otboo.domain.directmessage.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class DirectMessageException extends OtbooException {

    public DirectMessageException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }

    public DirectMessageException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }
}
