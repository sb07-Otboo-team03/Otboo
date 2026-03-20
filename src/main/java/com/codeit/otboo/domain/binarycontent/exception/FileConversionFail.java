package com.codeit.otboo.domain.binarycontent.exception;


import com.codeit.otboo.global.exception.ErrorCode;

import java.util.Map;

public class FileConversionFail extends BinaryContentException {

    public FileConversionFail(ErrorCode errorCode,
                              Map<String, String> details) {
        super(errorCode, details);
    }

    public FileConversionFail(ErrorCode errorCode) {
        super(errorCode);
    }
}
