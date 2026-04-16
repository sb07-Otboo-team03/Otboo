package com.codeit.otboo.global.security.jwt.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class JwtInvalidSignatureException extends JwtException{
    public JwtInvalidSignatureException() {
        super(ErrorCode.INVALID_SIGNATURE,
                null,
                HttpStatus.UNAUTHORIZED);
    }
}
