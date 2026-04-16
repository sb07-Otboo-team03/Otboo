package com.codeit.otboo.domain.profile.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ProfileException extends OtbooException {
    public ProfileException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }

    public ProfileException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
