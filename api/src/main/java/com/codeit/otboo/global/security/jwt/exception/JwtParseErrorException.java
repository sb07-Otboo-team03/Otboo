package com.codeit.otboo.global.security.jwt.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class JwtParseErrorException extends JwtException{
    public JwtParseErrorException() {
        super(ErrorCode.PARSE_ERROR,
                null,
                HttpStatus.BAD_REQUEST);
    }
}
