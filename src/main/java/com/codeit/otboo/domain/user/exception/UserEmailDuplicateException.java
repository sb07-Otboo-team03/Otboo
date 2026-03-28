package com.codeit.otboo.domain.user.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class UserEmailDuplicateException extends UserException {
    public UserEmailDuplicateException(String email) {
        super(ErrorCode.USER_ALREADY_EXISTS_EMAIL,
                Map.of("userEmail", email),
                HttpStatus.NOT_FOUND);
    }

    public UserEmailDuplicateException() {
        super(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
