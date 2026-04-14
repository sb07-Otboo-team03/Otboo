package com.codeit.otboo.domain.clothes.attribute.attributevalue.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ClothesAttributeValueException extends OtbooException {
    public ClothesAttributeValueException(ErrorCode errorCode,
                                          Map<String, String> details,
                                          HttpStatus status) {
        super(errorCode, details, status);
    }

    public ClothesAttributeValueException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
