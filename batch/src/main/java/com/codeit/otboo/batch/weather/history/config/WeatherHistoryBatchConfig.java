package com.codeit.otboo.batch.weather.history.config;

import com.codeit.otboo.batch.weather.common.listener.WeatherBatchJobListener;
import com.codeit.otboo.batch.weather.common.listener.WeatherBatchStepListener;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.domain.weather.repository.YesterdayHourlyWeatherRepository;
import com.codeit.otboo.global.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class WeatherHistoryBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeatherBatchJobListener weatherBatchJobListener;
    private final WeatherBatchStepListener weatherBatchStepListener;
    private final WeatherRepository weatherRepository;
    private final YesterdayHourlyWeatherRepository yesterdayRepository;
    private final TimeProvider timeProvider;

    // Job
    @Bean
    public Job deleteYesterdayWeatherJob(
            Step deleteYesterdayWeatherStep
    ) {
        return new JobBuilder("deleteYesterdayWeatherJob", jobRepository)
                .start(deleteYesterdayWeatherStep)
                .listener(weatherBatchJobListener)
                .build();
    }

    // 어제 데이터 삭제 (Tasklet)
    @Bean
    public Step deleteYesterdayWeatherStep() {
        return new StepBuilder("deleteYesterdayWeatherStep", jobRepository)
                .tasklet(deleteYesterdayWeatherTasklet(), transactionManager)
                .listener(weatherBatchStepListener)
                .build();
    }

    // Delete Tasklet
    // Weather 테이블: 오늘 이전 데이터 삭제 (어제 포함)
    // YesterdayHourlyWeather 테이블: 어제 이전 데이터 삭제 (그제 포함)
    @Bean
    @StepScope
    public Tasklet deleteYesterdayWeatherTasklet() {
        return (contribution, chunkContext) -> {

            LocalDate today = timeProvider.nowDate();
            LocalDate yesterday = today.minusDays(1);

            weatherRepository.deleteByForecastedAtBefore(today.atStartOfDay()); // 오늘 이전 데이터 삭제
            yesterdayRepository.deleteByDateBefore(yesterday); // 어제 데이터 저장하는 테이블에서도 어제 이전 데이터 삭제

            return RepeatStatus.FINISHED;
        };
    }
}