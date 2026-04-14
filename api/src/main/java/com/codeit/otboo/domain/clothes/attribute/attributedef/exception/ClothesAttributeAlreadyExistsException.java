package com.codeit.otboo.domain.clothes.attribute.attributedef.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ClothesAttributeAlreadyExistsException extends ClothesAttributeException {
    public ClothesAttributeAlreadyExistsException() {
        super(ErrorCode.CLOTHES_ATTRIBUTE_ALREADY_EXISTS,
                Map.of(),
                HttpStatus.BAD_REQUEST);
    }

    public ClothesAttributeAlreadyExistsException(ErrorCode errorCode, HttpStatus status) {
        super(ErrorCode.CLOTHES_ATTRIBUTE_ALREADY_EXISTS,
                HttpStatus.BAD_REQUEST);
    }
}
