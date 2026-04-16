package com.codeit.otboo.batch.weather.forecast.processor;

import com.codeit.otboo.batch.weather.forecast.model.ForecastBatchResult;
import com.codeit.otboo.batch.weather.forecast.model.ForecastTarget;
import com.codeit.otboo.batch.weather.forecast.service.WeatherForecastBatchService;
import com.codeit.otboo.global.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@StepScope
@RequiredArgsConstructor
public class ForecastTargetProcessor implements ItemProcessor<ForecastTarget, ForecastBatchResult> {

    private final WeatherForecastBatchService weatherForecastBatchService;
    private final TimeProvider timeProvider;

    @Override
    public ForecastBatchResult process(ForecastTarget item) {
        String baseDate = timeProvider.nowDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getBaseTimeForWeather();

        return weatherForecastBatchService.collect(
                item.x(),
                item.y(),
                baseDate,
                baseTime
        );
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