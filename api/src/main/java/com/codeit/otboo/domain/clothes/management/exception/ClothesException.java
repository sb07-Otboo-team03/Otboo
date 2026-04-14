package com.codeit.otboo.domain.clothes.management.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ClothesException extends OtbooException {
    public ClothesException(ErrorCode errorCode,
                                  Map<String, String> details,
                                  HttpStatus status) {
        super(errorCode, details, status);
    }

    public ClothesException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
