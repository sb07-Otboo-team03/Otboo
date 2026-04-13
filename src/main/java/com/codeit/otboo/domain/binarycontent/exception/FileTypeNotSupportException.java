package com.codeit.otboo.domain.binarycontent.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class FileTypeNotSupportException extends BinaryContentException {
    public FileTypeNotSupportException(){
        super(ErrorCode.INVALID_FILE_TYPE, HttpStatus.BAD_REQUEST);
    }
    public FileTypeNotSupportException(String requestType, String supportType) {
        super(
                ErrorCode.INVALID_FILE_TYPE,
                Map.of(
                        "requestedType", requestType,
                        "supportedTypes", supportType
                ),
                HttpStatus.BAD_REQUEST
        );
    }
}
