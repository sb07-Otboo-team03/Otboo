package com.codeit.otboo.batch.weather.common.exception;

public class BatchJobExecutionException extends BatchException {

    public BatchJobExecutionException(String jobName, Throwable cause) {
        super("배치 Job 실행에 실패했습니다. jobName=" + jobName, cause);
    }

    public BatchJobExecutionException(String jobName) {
        super("배치 Job 실행에 실패했습니다. jobName=" + jobName);
    }
}
