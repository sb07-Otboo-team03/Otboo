package com.codeit.otboo.batch.weather.forecast.processor;

import com.codeit.otboo.batch.weather.forecast.model.ForecastBatchResult;
import com.codeit.otboo.batch.weather.forecast.model.ForecastTarget;
import com.codeit.otboo.batch.weather.forecast.service.WeatherForecastBatchService;
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