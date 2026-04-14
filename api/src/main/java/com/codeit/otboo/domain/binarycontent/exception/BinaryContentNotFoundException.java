package com.codeit.otboo.domain.binarycontent.exception;


import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class BinaryContentNotFoundException extends BinaryContentException {
    public BinaryContentNotFoundException(){
        super(ErrorCode.BINARY_CONTENT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    public BinaryContentNotFoundException(UUID binaryContentId) {
        super(
            ErrorCode.BINARY_CONTENT_NOT_FOUND,
            Map.of("binaryContentId", binaryContentId.toString()),
            HttpStatus.NOT_FOUND
        );
    }
}
