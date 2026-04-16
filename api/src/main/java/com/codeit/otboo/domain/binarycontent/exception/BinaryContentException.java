package com.codeit.otboo.domain.binarycontent.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class BinaryContentException extends OtbooException {

    public BinaryContentException(ErrorCode errorCode,
                                  Map<String, String> details,
                                  HttpStatus status) {
        super(errorCode, details, status);
    }

    public BinaryContentException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode, status);
    }
}
