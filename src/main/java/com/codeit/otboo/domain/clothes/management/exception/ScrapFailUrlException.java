package com.codeit.otboo.domain.clothes.management.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ScrapFailUrlException extends ClothesException{
    public ScrapFailUrlException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    public ScrapFailUrlException(String requestUrl) {
        super(
                ErrorCode.INTERNAL_SERVER_ERROR,
                Map.of("requestUrl", requestUrl),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
