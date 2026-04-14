package com.codeit.otboo.domain.clothes.attribute.attributedef.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class ClothesAttributeValueDuplicateExceptionException extends ClothesAttributeException {
    public ClothesAttributeValueDuplicateExceptionException(UUID definition_id) {
        super(ErrorCode.CLOTHES_ATTRIBUTE_VALUE_DUPLICATE,
                Map.of(),
                HttpStatus.BAD_REQUEST);
    }

    public ClothesAttributeValueDuplicateExceptionException() {
        super(ErrorCode.CLOTHES_ATTRIBUTE_VALUE_DUPLICATE,
                HttpStatus.BAD_REQUEST);
    }
}
