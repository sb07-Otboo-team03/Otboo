package com.codeit.otboo.domain.clothes.management.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class ClothesNotFoundException extends ClothesException{
    public ClothesNotFoundException(){
        super(ErrorCode.CLOTHES_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    public ClothesNotFoundException(UUID clothesId) {
        super(
                ErrorCode.CLOTHES_NOT_FOUND,
                Map.of("clothesId", clothesId.toString()),
                HttpStatus.NOT_FOUND
        );
    }
}
