package com.codeit.otboo.domain.clothes.attribute.attributedef.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class ClothesAttributeValueEmptyException extends ClothesAttributeException {
    public ClothesAttributeValueEmptyException(UUID definition_id) {
        super(ErrorCode.CLOTHES_ATTRIBUTE_VALUE_IS_EMPTY,
                Map.of(),
                HttpStatus.BAD_REQUEST);
    }

    public ClothesAttributeValueEmptyException() {
        super(ErrorCode.CLOTHES_ATTRIBUTE_VALUE_IS_EMPTY,
                HttpStatus.BAD_REQUEST);
    }
}
