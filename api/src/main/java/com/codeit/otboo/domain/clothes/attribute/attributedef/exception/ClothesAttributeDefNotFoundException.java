package com.codeit.otboo.domain.clothes.attribute.attributedef.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class ClothesAttributeDefNotFoundException extends ClothesAttributeException {
    public ClothesAttributeDefNotFoundException(UUID definition_id) {
        super(ErrorCode.CLOTHES_ATTRIBUTE_DEFINITION_NOT_FOUND,
                Map.of("clothesAttributeDefId", definition_id.toString()),
                HttpStatus.BAD_REQUEST);
    }

    public ClothesAttributeDefNotFoundException() {
        super(
                ErrorCode.CLOTHES_ATTRIBUTE_DEFINITION_NOT_FOUND,
                HttpStatus.BAD_REQUEST
        );
    }
}