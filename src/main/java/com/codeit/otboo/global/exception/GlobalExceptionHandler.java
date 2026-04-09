package com.codeit.otboo.global.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
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

    // Validation에 대한 에러 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        Map<String, String> details = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> Objects.requireNonNullElse(
                                fe.getDefaultMessage(),
                                "알려지지 않은 에러"
                        )
                ));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exceptionName(errorCode.name())
                .message(errorCode.getMessage())
                .details(details)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {

        ErrorResponse error = ErrorResponse.builder()
                .exceptionName(e.getClass().getSimpleName())
                .message("자격 증명에 실패하였습니다.")
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exceptionName(e.getClass().getSimpleName())
                .message("요청하신 작업에 대한 권한이 없습니다.")
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .exceptionName("MISSING_REQUEST_PARAMETER")
                .message("필수 파라미터가 없습니다.")
                .details(Map.of("parameter", e.getParameterName()))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingCookie(MissingRequestCookieException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .exceptionName("MISSING_COOKIE")
                .message("필수 쿠키가 존재하지 않습니다.")
                .details(Map.of(
                        "cookieName", e.getCookieName()
                ))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        // 500 에러는 예상하지 못한 에러이기에, stack Trace 로그 출력
        log.error("Unhandled exception", e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        Map<String, String> details = Map.of(
                "exceptionClass", e.getClass().getSimpleName(),
                "exceptionMessage", Objects.requireNonNullElse(e.getMessage(), "No message available")
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exceptionName(errorCode.name())
                .message(errorCode.getMessage())
                .details(details)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
