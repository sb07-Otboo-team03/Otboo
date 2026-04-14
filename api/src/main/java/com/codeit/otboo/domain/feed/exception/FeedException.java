package com.codeit.otboo.domain.feed.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class FeedException extends OtbooException {
    public FeedException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }

    public FeedException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
