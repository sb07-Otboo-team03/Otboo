package com.codeit.otboo.domain.weather.batch.config;

import com.codeit.otboo.domain.weather.batch.dto.ForecastBatchResult;
import com.codeit.otboo.domain.weather.batch.dto.ForecastTarget;
import com.codeit.otboo.domain.weather.batch.listener.WeatherBatchJobListener;
import com.codeit.otboo.domain.weather.batch.listener.WeatherBatchStepListener;
import com.codeit.otboo.domain.weather.batch.processor.ForecastTargetProcessor;
import com.codeit.otboo.domain.weather.batch.reader.ForecastTargetReader;
import com.codeit.otboo.domain.weather.batch.writer.WeatherForecastWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeatherForecastJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeatherBatchJobListener weatherBatchJobListener;
    private final WeatherBatchStepListener weatherBatchStepListener;
    private final ForecastTargetReader forecastTargetReader;
    private final ForecastTargetProcessor forecastTargetProcessor;
    private final WeatherForecastWriter weatherForecastWriter;

    @Bean
    public Job weatherForecastCollectionJob() {
        return new JobBuilder("weatherForecastCollectionJob", jobRepository)
                .start(weatherForecastCollectionStep())
                .listener(weatherBatchJobListener)
                .build();
    }

    @Bean
    public Step weatherForecastCollectionStep() {
        return new StepBuilder("weatherForecastCollectionStep", jobRepository)
                .<ForecastTarget, ForecastBatchResult>chunk(10, transactionManager)
                .reader(forecastTargetReader)
                .processor(forecastTargetProcessor)
                .writer(weatherForecastWriter)
                .listener(weatherBatchStepListener)
                .build();
    }
}