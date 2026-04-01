package com.codeit.otboo.domain.binarycontent.exception;


import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class FileConversionException extends BinaryContentException {

    public FileConversionException() {
        super(ErrorCode.FILE_CONVERSION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
