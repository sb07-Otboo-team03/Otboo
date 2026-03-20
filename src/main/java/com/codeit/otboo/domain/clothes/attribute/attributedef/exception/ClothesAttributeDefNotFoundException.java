package com.codeit.otboo.domain.clothes.attribute.attributedef.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ClothesAttributeDefNotFoundException extends ClothesAttributeException{
    public ClothesAttributeDefNotFoundException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }

    public ClothesAttributeDefNotFoundException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
