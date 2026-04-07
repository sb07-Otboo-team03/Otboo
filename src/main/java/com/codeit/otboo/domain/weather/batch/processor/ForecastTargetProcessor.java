package com.codeit.otboo.domain.weather.batch.processor;

import com.codeit.otboo.domain.weather.batch.dto.ForecastBatchResult;
import com.codeit.otboo.domain.weather.batch.dto.ForecastTarget;
import com.codeit.otboo.domain.weather.service.WeatherForecastBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class ForecastTargetProcessor implements ItemProcessor<ForecastTarget, ForecastBatchResult> {

    private final WeatherForecastBatchService weatherForecastBatchService;

    @Value("#{jobParameters['baseDate']}")
    private String baseDate;

    @Value("#{jobParameters['baseTime']}")
    private String baseTime;

    @Override
    public ForecastBatchResult process(ForecastTarget item) {
        return weatherForecastBatchService.collect(
                item.x(),
                item.y(),
                baseDate,
                baseTime
        );
    }
}