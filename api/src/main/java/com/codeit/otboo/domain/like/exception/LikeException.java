package com.codeit.otboo.domain.like.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class LikeException extends OtbooException {

    public LikeException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }

    public LikeException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
