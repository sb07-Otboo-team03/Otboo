package com.codeit.otboo.domain.binarycontent.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class BinaryContentException extends OtbooException {

    public BinaryContentException(ErrorCode errorCode,
                                  Map<String, String> details) {
        super(errorCode, details, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public BinaryContentException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
