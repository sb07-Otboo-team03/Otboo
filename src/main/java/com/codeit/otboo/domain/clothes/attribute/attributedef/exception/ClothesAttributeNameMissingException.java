package com.codeit.otboo.domain.clothes.attribute.attributedef.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ClothesAttributeNameMissingException extends ClothesAttributeException {
    public ClothesAttributeNameMissingException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }

    public ClothesAttributeNameMissingException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
