package com.codeit.otboo.domain.clothes.attribute.attributedef.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class ClothesAttributeSelectableValueMissingException extends ClothesAttributeException {
    public ClothesAttributeSelectableValueMissingException(UUID definition_id) {
        super(ErrorCode.CLOTHES_SELECTABLE_VALUE_MISSING,
                Map.of("clothesAttributeDefId", definition_id.toString()),
                HttpStatus.BAD_REQUEST
                );
    }

    public ClothesAttributeSelectableValueMissingException() {
        super(ErrorCode.CLOTHES_SELECTABLE_VALUE_MISSING,
                HttpStatus.BAD_REQUEST);
    }
}
