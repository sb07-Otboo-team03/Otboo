package com.codeit.otboo.global.security.jwt.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class JwtExpiredTokenException extends JwtException {

    public JwtExpiredTokenException(String refreshToken) {
        super(ErrorCode.EXPIRED_TOKEN,
                Map.of("refreshToken", refreshToken),
                HttpStatus.UNAUTHORIZED);
    }

    public JwtExpiredTokenException() {
        super(ErrorCode.EXPIRED_TOKEN, HttpStatus.UNAUTHORIZED);
    }

}