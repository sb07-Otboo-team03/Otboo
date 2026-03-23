package com.codeit.otboo.global.exception.user;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class UserException extends OtbooException {

    public UserException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }

    public UserException(ErrorCode errorCode,
        Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }
}
