package com.codeit.otboo.global.security.jwt.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class JwtInvalidTokenTypeException extends JwtException{
    public JwtInvalidTokenTypeException() {
        super(ErrorCode.INVALID_TOKEN_TYPE,
                null,
                HttpStatus.UNAUTHORIZED);
    }
}
