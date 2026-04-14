package com.codeit.otboo.batch.weather.common.exception;

import lombok.Getter;

@Getter
public class BatchException extends RuntimeException {

    public BatchException(String message) {
        super(message);
    }

    public BatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
