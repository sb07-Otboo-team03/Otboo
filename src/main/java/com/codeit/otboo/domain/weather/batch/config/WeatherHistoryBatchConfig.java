package com.codeit.otboo.domain.weather.batch.config;

import com.codeit.otboo.domain.weather.batch.listener.WeatherBatchJobListener;
import com.codeit.otboo.domain.weather.batch.listener.WeatherBatchStepListener;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.domain.weather.repository.YesterdayHourlyWeatherRepository;
import com.codeit.otboo.global.util.TimeProvider;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WeatherHistoryBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeatherBatchJobListener weatherBatchJobListener;
    private final WeatherBatchStepListener weatherBatchStepListener;
    private final EntityManagerFactory entityManagerFactory;
    private final WeatherRepository weatherRepository;
    private final YesterdayHourlyWeatherRepository yesterdayRepository;
    private final TimeProvider timeProvider;

    // Job
    @Bean
    public Job deleteYesterdayWeatherJob(
            Step migrateYesterdayWeatherStep,
            Step deleteYesterdayWeatherStep
    ) {
        return new JobBuilder("deleteYesterdayWeatherJob", jobRepository)
                .start(migrateYesterdayWeatherStep)
                .next(deleteYesterdayWeatherStep)
                .listener(weatherBatchJobListener)
                .build();
    }

    // Step 1: 어제 데이터 저장 (chunk)
    @Bean
    public Step migrateYesterdayWeatherStep() {
        return new StepBuilder("migrateYesterdayWeatherStep", jobRepository)
                .<Weather, YesterdayHourlyWeather>chunk(500, transactionManager)
                .reader(weatherItemReader())
                .processor(weatherItemProcessor())
                .writer(yesterdayItemWriter())
                .listener(weatherBatchStepListener)
                .build();
    }

    // Step 2: 어제 데이터 삭제 (Tasklet)
    @Bean
    public Step deleteYesterdayWeatherStep() {
        return new StepBuilder("deleteYesterdayWeatherStep", jobRepository)
                .tasklet(deleteYesterdayWeatherTasklet(), transactionManager)
                .listener(weatherBatchStepListener)
                .build();
    }

    // Reader (Paging 기반)
    @Bean
    @StepScope
    public JpaPagingItemReader<Weather> weatherItemReader() {

        LocalDate today = timeProvider.nowDate();
        LocalDate yesterday = today.minusDays(1);

        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = today.atStartOfDay();

        JpaPagingItemReader<Weather> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setPageSize(500);

        reader.setQueryString("""
            SELECT w FROM Weather w
            WHERE w.forecastedAt >= :start
              AND w.forecastedAt < :end
            ORDER BY w.id
        """);

        Map<String, Object> params = Map.of(
                "start", start,
                "end", end
        );

        reader.setParameterValues(params);

        return reader;
    }

    // Processor
    @Bean
    public ItemProcessor<Weather, YesterdayHourlyWeather> weatherItemProcessor() {
        return weather -> new YesterdayHourlyWeather(
                weather.getX(),
                weather.getY(),
                weather.getForecastAt().toLocalDate(),
                weather.getForecastAt().toLocalTime(),
                weather.getTemperatureCurrent(),
                weather.getHumidityCurrent()
        );
    }

    // Writer
    @Bean
    public ItemWriter<YesterdayHourlyWeather> yesterdayItemWriter() {
        return items -> yesterdayRepository.saveAll(items);
    }

    // Delete Tasklet (어제 데이터만 삭제)
    @Bean
    @StepScope
    public Tasklet deleteYesterdayWeatherTasklet() {
        return (contribution, chunkContext) -> {

            LocalDate today = timeProvider.nowDate();
            LocalDate yesterday = today.minusDays(1);

            LocalDateTime start = yesterday.atStartOfDay();
            LocalDateTime end = today.atStartOfDay();

            weatherRepository.deleteByForecastedAtBetween(start, end);

            return RepeatStatus.FINISHED;
        };
    }
}