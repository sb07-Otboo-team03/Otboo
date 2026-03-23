package com.codeit.otboo.global.exception.user;

import com.codeit.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends UserException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND,
            HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(UUID userId) {
        super(ErrorCode.USER_NOT_FOUND,
            Map.of("userId : ", userId.toString()),
            HttpStatus.NOT_FOUND);
    }
}