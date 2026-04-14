package com.codeit.otboo.batch.weather.common.listener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherBatchStepListener implements StepExecutionListener {

    private final MeterRegistry meterRegistry;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(
                "[BATCH-STEP-START] stepName={}, jobName={}",
                stepExecution.getStepName(),
                stepExecution.getJobExecution().getJobInstance().getJobName()
        );
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        String stepName = stepExecution.getStepName();
        BatchStatus status = stepExecution.getStatus();

        long durationMs = calculateDurationMs(
                stepExecution.getStartTime(),
                stepExecution.getEndTime()
        );

        long skipCount =
                stepExecution.getReadSkipCount()
                        + stepExecution.getProcessSkipCount()
                        + stepExecution.getWriteSkipCount();

        Timer.builder("weather.batch.step.duration")
                .description("날씨 배치 Step 실행 시간")
                .tag("jobName", jobName)
                .tag("stepName", stepName)
                .tag("status", status.name())
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        meterRegistry.counter(
                "weather.batch.step.read.count",
                "jobName", jobName,
                "stepName", stepName
        ).increment(stepExecution.getReadCount());

        meterRegistry.counter(
                "weather.batch.step.write.count",
                "jobName", jobName,
                "stepName", stepName
        ).increment(stepExecution.getWriteCount());

        meterRegistry.counter(
                "weather.batch.step.skip.count",
                "jobName", jobName,
                "stepName", stepName
        ).increment(skipCount);

        log.info(
                "[BATCH-STEP-END] jobName={}, stepName={}, status={}, durationMs={}, readCount={}, writeCount={}, skipCount={}",
                jobName,
                stepName,
                status,
                durationMs,
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                skipCount
        );

        return stepExecution.getExitStatus();
    }

    private long calculateDurationMs(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0L;
        }
        return Duration.between(startTime, endTime).toMillis();
    }
}