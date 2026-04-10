package com.codeit.otboo.batch.weather.common.exception;

import com.codeit.otboo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class BatchJobExecutionException extends BatchException {

    public BatchJobExecutionException(String jobName, Throwable cause) {
        super(
                ErrorCode.BATCH_JOB_EXECUTION_FAILED,
                Map.of(
                        "jobName", jobName,
                        "cause", cause.getClass().getSimpleName()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        initCause(cause); // 실제로 발생한 에러 원인
    }

    public BatchJobExecutionException(String jobName) {
        super(
                ErrorCode.BATCH_JOB_EXECUTION_FAILED,
                Map.of("jobName", jobName),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
