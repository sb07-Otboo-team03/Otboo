package com.codeit.otboo.global.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class KakaoApiException extends OtbooException {
    public KakaoApiException(String message) {
        super(
                ErrorCode.KAKAO_API_ERROR,
                Map.of("reason", message),
                HttpStatus.BAD_GATEWAY
        );
    }
}
