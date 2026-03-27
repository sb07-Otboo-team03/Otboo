package com.codeit.otboo.global.security.jwt.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class JwtInvalidTokenTypeException extends JwtException{
    public JwtInvalidTokenTypeException() {
        super(ErrorCode.INVALID_TOKEN_TYPE,
                null,
                HttpStatus.UNAUTHORIZED);
    }
}
