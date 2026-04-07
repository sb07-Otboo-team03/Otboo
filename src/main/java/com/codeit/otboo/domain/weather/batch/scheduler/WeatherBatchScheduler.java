package com.codeit.otboo.domain.weather.batch.scheduler;

import com.codeit.otboo.domain.weather.batch.exception.BatchJobExecutionException;
import com.codeit.otboo.global.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job weatherForecastCollectionJob;
    private final Job deleteYesterdayWeatherJob;
    private final Job weatherAlertJob;
    private final TimeProvider timeProvider;

    @Scheduled(cron = "0 15 2,5,8,11,14,17,20,23 * * *")
    public void runWeatherForecastBatch() {
        String baseDate = timeProvider.nowDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getBaseTimeForWeather();

        Map<String, JobParameter<?>> jobParameterMap = new HashMap<>();
        jobParameterMap.put("baseDate", new JobParameter<>(baseDate, String.class));
        jobParameterMap.put("baseTime", new JobParameter<>(baseTime, String.class));
        jobParameterMap.put(
                "requestedAt",
                new JobParameter<>(timeProvider.nowDateTime().toString(), String.class)
        );

        JobParameters jobParameters = new JobParameters(jobParameterMap);

        try {
            log.info("날씨 예보 배치 실행 시작 - baseDate: {}, baseTime: {}", baseDate, baseTime);

            jobLauncher.run(weatherForecastCollectionJob, jobParameters);

            log.info("날씨 예보 배치 실행 요청 완료 - baseDate: {}, baseTime: {}", baseDate, baseTime);
        } catch (Exception e) {
            log.error("날씨 예보 배치 실행 실패 - baseDate: {}, baseTime: {}", baseDate, baseTime, e);
            throw new BatchJobExecutionException("weatherForecastCollectionJob", e);
        }
    }

    /**
     * 어제 날씨 이관 및 삭제 배치 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runDeleteYesterdayWeatherBatch() {
        String targetDate = timeProvider.nowDate().minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestedAt", timeProvider.nowDateTime().toString())
                .toJobParameters();

        try {
            log.info("어제 날씨 이관/삭제 배치 실행 시작 - targetDate: {}", targetDate);
            jobLauncher.run(deleteYesterdayWeatherJob, jobParameters);
            log.info("어제 날씨 이관/삭제 배치 실행 완료 - targetDate: {}", targetDate);
        } catch (Exception e) {
            log.error("어제 날씨 이관/삭제 배치 실행 실패 - targetDate: {}", targetDate, e);
            throw new BatchJobExecutionException("deleteYesterdayWeatherJob", e);
        }
    }

    /**
     * 오전 6시마다 지역 별로 날씨 정보에 따라 알림 생성
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void runWeatherAlertBatch() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestedAt", timeProvider.nowDateTime().toString())
                .toJobParameters();

        try {
            log.info("날씨 알림 배치 실행 시작");
            jobLauncher.run(weatherAlertJob, jobParameters);
            log.info("날씨 알림 배치 실행 요청 완료");
        } catch (Exception e) {
            log.error("날씨 알림 배치 실행 실패", e);
            throw new BatchJobExecutionException("weatherAlertJob", e);
        }
    }

    private String getBaseTimeForWeather() {
        int hour = timeProvider.nowTime().getHour();

        if (hour < 2) return "2300";
        else if (hour < 5) return "0200";
        else if (hour < 8) return "0500";
        else if (hour < 11) return "0800";
        else if (hour < 14) return "1100";
        else if (hour < 17) return "1400";
        else if (hour < 20) return "1700";
        else if (hour < 23) return "2000";
        else return "2300";
    }
}