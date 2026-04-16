package com.codeit.otboo.domain.clothes.management.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ScrapFailUrlException extends ClothesException{
    public ScrapFailUrlException() {
        super(ErrorCode.SCRAP_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    public ScrapFailUrlException(String requestUrl) {
        super(
                ErrorCode.SCRAP_FAIL,
                Map.of("requestUrl", requestUrl),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
