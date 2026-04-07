package com.codeit.otboo.domain.weather.batch.config;

import com.codeit.otboo.domain.weather.batch.dto.RegionAlertResult;
import com.codeit.otboo.domain.weather.batch.dto.RegionAlertTarget;
import com.codeit.otboo.domain.weather.batch.listener.WeatherBatchJobListener;
import com.codeit.otboo.domain.weather.batch.listener.WeatherBatchStepListener;
import com.codeit.otboo.domain.weather.batch.processor.RegionAlertProcessor;
import com.codeit.otboo.domain.weather.batch.reader.RegionAlertTargetReader;
import com.codeit.otboo.domain.weather.batch.writer.RegionAlertWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeatherAlertJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeatherBatchJobListener weatherBatchJobListener;
    private final WeatherBatchStepListener weatherBatchStepListener;
    private final RegionAlertTargetReader regionAlertTargetReader;
    private final RegionAlertProcessor regionAlertProcessor;
    private final RegionAlertWriter regionAlertWriter;

    @Bean
    public Job weatherAlertJob() {
        return new JobBuilder("weatherAlertJob", jobRepository)
                .start(weatherAlertStep())
                .listener(weatherBatchJobListener)
                .build();
    }

    @Bean
    public Step weatherAlertStep() {
        return new StepBuilder("weatherAlertStep", jobRepository)
                .<RegionAlertTarget, RegionAlertResult>chunk(50, transactionManager)
                .reader(weatherAlertReader())
                .processor(regionAlertProcessor)
                .writer(regionAlertWriter)
                .listener(weatherBatchStepListener)
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<RegionAlertTarget> weatherAlertReader() {
        return regionAlertTargetReader.reader();
    }
}
