package com.codeit.otboo.domain.clothes.attribute.attributedef.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ClothesAttributeException extends OtbooException{

    public ClothesAttributeException(ErrorCode errorCode, Map<String, String> map, HttpStatus status) {
        super(errorCode, map, status);
    }

    public ClothesAttributeException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
