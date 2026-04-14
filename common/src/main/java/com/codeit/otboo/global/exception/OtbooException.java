package com.codeit.otboo.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class OtbooException extends RuntimeException {
    private final LocalDateTime timestamp;
    private final ErrorCode errorCode;
    private final Map<String, String> details;
    private final HttpStatus status;

    public OtbooException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.details = map;
        this.status = status;
    }

    public OtbooException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.details = null;
    }
}
