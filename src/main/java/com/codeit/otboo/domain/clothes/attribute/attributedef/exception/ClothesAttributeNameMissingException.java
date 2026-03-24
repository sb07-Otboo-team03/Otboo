package com.codeit.otboo.domain.clothes.attribute.attributedef.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class ClothesAttributeNameMissingException extends ClothesAttributeException {
    public ClothesAttributeNameMissingException(UUID definition_id) {
        super(ErrorCode.CLOTHES_ATTRIBUTE_NAME_MISSING,
                Map.of(),
                HttpStatus.BAD_REQUEST);
    }

    public ClothesAttributeNameMissingException() {
        super(ErrorCode.CLOTHES_ATTRIBUTE_NAME_MISSING,
                HttpStatus.BAD_REQUEST);
    }
}
