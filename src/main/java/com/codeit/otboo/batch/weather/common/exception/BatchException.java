package com.codeit.otboo.batch.weather.common.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class BatchException extends OtbooException {

    public BatchException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }

    public BatchException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
