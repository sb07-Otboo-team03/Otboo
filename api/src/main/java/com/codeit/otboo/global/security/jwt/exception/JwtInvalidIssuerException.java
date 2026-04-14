package com.codeit.otboo.global.security.jwt.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class JwtInvalidIssuerException extends JwtException{
    public JwtInvalidIssuerException() {
        super(ErrorCode.INVALID_ISSUER,
                null,
                HttpStatus.UNAUTHORIZED);
    }
}
