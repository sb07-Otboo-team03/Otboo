package com.codeit.otboo.domain.weather.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class KmaApiInvalidResponseException extends KmaApiException {

    public KmaApiInvalidResponseException(String reason) {
        super(
                ErrorCode.KMA_API_INVALID_RESPONSE,
                Map.of("reason", reason),
                HttpStatus.BAD_GATEWAY
        );
    }

    public KmaApiInvalidResponseException() {
        super(
                ErrorCode.KMA_API_INVALID_RESPONSE,
                HttpStatus.BAD_GATEWAY
        );
    }
}
