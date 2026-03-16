package com.codeit.otboo.global.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    @ExceptionHandler(OtbooException.class)
    public ResponseEntity<ErrorResponse> OtbooException(OtbooException e) {
        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exceptionName(errorCode.name())
                .message(errorCode.getMessage())
                .details(e.getDetails())
                .build();

        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }
}
