package com.codeit.otboo.batch.weather.forecast.writer;

import com.codeit.otboo.batch.weather.forecast.model.ForecastBatchResult;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.service.WeatherUpsertService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WeatherForecastWriter implements ItemWriter<ForecastBatchResult> {

    private final WeatherUpsertService weatherUpsertService;

    @Override
    public void write(Chunk<? extends ForecastBatchResult> chunk) {
        List<Weather> allWeathers = chunk.getItems().stream()
                .flatMap(result -> result.weathers().stream())
                .toList();

        weatherUpsertService.upsert(allWeathers);
    }
}