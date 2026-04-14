package com.codeit.otboo.domain.user.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class TemporaryPasswordException extends OtbooException {
    public TemporaryPasswordException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }

    public TemporaryPasswordException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
