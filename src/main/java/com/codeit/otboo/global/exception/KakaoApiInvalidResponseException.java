package com.codeit.otboo.global.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class KakaoApiInvalidResponseException extends OtbooException {
    public KakaoApiInvalidResponseException(String reason) {
        super(
                ErrorCode.KAKAO_API_INVALID_RESPONSE,
                Map.of("reason", reason),
                HttpStatus.BAD_GATEWAY
        );
    }
}