package com.codeit.otboo.domain.user.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class TemporaryPasswordMailSendFailedException extends TemporaryPasswordException {
    public TemporaryPasswordMailSendFailedException() {
        super(ErrorCode.MAIL_SEND_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
