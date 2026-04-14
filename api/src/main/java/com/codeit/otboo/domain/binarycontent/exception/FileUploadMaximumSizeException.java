package com.codeit.otboo.domain.binarycontent.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class FileUploadMaximumSizeException extends BinaryContentException {
    public FileUploadMaximumSizeException(){
        super(ErrorCode.FILE_UPLOAD_MAXIMUM_SIZE, HttpStatus.BAD_REQUEST);
    }
    public FileUploadMaximumSizeException(long fileSize, long maximumSize) {
        super(
                ErrorCode.FILE_UPLOAD_MAXIMUM_SIZE,
                Map.of(
                        "upload size", String.valueOf(fileSize),
                        "maximum size", String.valueOf(maximumSize)
                ),
                HttpStatus.BAD_REQUEST
        );
    }
}
