package com.codeit.otboo.domain.user.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class TemporaryPasswordMailSendFailedException extends TemporaryPasswordException {
    public TemporaryPasswordMailSendFailedException() {
        super(ErrorCode.MAIL_SEND_FAIL, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
