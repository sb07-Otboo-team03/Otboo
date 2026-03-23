package com.codeit.otboo.global.exception.follow;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class FollowException extends OtbooException {

    public FollowException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }

    public FollowException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }
}
