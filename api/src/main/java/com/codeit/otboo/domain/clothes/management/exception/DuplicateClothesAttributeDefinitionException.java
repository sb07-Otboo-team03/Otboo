package com.codeit.otboo.domain.clothes.management.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class DuplicateClothesAttributeDefinitionException extends ClothesException {
    public DuplicateClothesAttributeDefinitionException() {
        super(ErrorCode.CLOTHES_DUPLICATED_VALUE, HttpStatus.CONFLICT);
    }
    public DuplicateClothesAttributeDefinitionException(UUID definitionId) {
        super(
            ErrorCode.CLOTHES_DUPLICATED_VALUE,
            Map.of("definitionId", definitionId.toString()),
            HttpStatus.CONFLICT
        );
    }
}
