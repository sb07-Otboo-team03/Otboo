package com.codeit.otboo.domain.binarycontent.exception;


import com.codeit.otboo.global.exception.ErrorCode;

import java.util.Map;

public class BinaryContentNotFoundException extends BinaryContentException {

    public BinaryContentNotFoundException(ErrorCode errorCode,
                                          Map<String, String> details) {
        super(errorCode, details);
    }

    public BinaryContentNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
