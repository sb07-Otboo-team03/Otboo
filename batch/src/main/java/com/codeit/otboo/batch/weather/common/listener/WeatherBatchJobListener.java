package com.codeit.otboo.batch.weather.common.listener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherBatchJobListener implements JobExecutionListener {

    private final MeterRegistry meterRegistry;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        log.info(
                "날씨 배치 시작 - jobName={}, jobParameters={}",
                jobName,
                jobExecution.getJobParameters()
        );
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        BatchStatus status = jobExecution.getStatus();

        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        long durationMs = calculateDurationMs(startTime, endTime);

        // 배치 실행 시간을 측정해서 metric으로 기록
        Timer.builder("weather.batch.job.duration")
                .description("날씨 배치 Job 실행 시간")
                .tag("jobName", jobName)
                .tag("status", status.name())
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        if (status == BatchStatus.COMPLETED) { // 배치 성공시 metric 기록
            meterRegistry.counter(
                    "weather.batch.job.success",
                    "jobName", jobName
            ).increment();
        } else { // 배치 실패시 metric 기록
            meterRegistry.counter(
                    "weather.batch.job.failure",
                    "jobName", jobName
            ).increment();
        }

        log.info(
                "날씨 배치 종료 - jobName={}, status={}, durationMs={}, jobParameters={}",
                jobName,
                status,
                durationMs,
                jobExecution.getJobParameters()
        );

        if (status == BatchStatus.FAILED) {
            jobExecution.getAllFailureExceptions().forEach(exception ->
                    log.error(
                            "날씨 배치 실패 - jobName={}, status={}, errorMessage={}",
                            jobName,
                            status,
                            exception.getMessage(),
                            exception
                    )
            );
        }
    }

    private long calculateDurationMs(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0L;
        }
        return Duration.between(startTime, endTime).toMillis();
    }
}
