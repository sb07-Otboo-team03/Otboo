package com.codeit.otboo.domain.clothes.attribute.attributevalue.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class ClothesAttributeValueNotFoundException extends ClothesAttributeValueException {
    public ClothesAttributeValueNotFoundException(){
        super(ErrorCode.CLOTHES_ATTRIBUTE_VALUES_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public ClothesAttributeValueNotFoundException(UUID attributeValueId){
        super(
                ErrorCode.CLOTHES_ATTRIBUTE_VALUES_NOT_FOUND,
                Map.of("attributeValueId", attributeValueId.toString()),
                HttpStatus.NOT_FOUND
        );
    }
    public ClothesAttributeValueNotFoundException(UUID definitionId, String value){
        super(
                ErrorCode.CLOTHES_ATTRIBUTE_VALUES_NOT_FOUND,
                Map.of(
                        "definitionId", definitionId.toString(),
                        "value", value
                ),
                HttpStatus.NOT_FOUND
        );
    }
}
