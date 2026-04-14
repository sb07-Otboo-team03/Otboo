package com.codeit.otboo.domain.weather.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class KmaApiErrorException extends KmaApiException {

    public KmaApiErrorException(String resultCode, String resultMsg) {
        super(
                ErrorCode.KMA_API_ERROR,
                Map.of(
                        "resultCode", resultCode,
                        "resultMsg", resultMsg
                ),
                HttpStatus.BAD_GATEWAY
        );
    }

    public KmaApiErrorException() {
        super(
                ErrorCode.KMA_API_ERROR,
                HttpStatus.BAD_GATEWAY
        );
    }
}
