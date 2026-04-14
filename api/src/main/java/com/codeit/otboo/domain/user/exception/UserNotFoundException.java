package com.codeit.otboo.domain.user.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends UserException {

    public UserNotFoundException(UUID userId) {
        super(ErrorCode.USER_NOT_FOUND,
                Map.of("userId", userId.toString()),
                HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String email) {
        super(ErrorCode.USER_NOT_FOUND,
                Map.of("email", email),
                HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
