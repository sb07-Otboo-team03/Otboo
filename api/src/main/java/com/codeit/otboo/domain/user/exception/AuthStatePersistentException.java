package com.codeit.otboo.domain.user.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class AuthStatePersistentException extends AuthException {
    public AuthStatePersistentException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR,
                Map.of("인증 저장 실패", ""),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
