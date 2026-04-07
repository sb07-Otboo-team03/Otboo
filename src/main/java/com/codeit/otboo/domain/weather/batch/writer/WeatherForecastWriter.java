package com.codeit.otboo.domain.weather.batch.writer;

import com.codeit.otboo.domain.weather.batch.dto.ForecastBatchResult;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.service.WeatherForecastUpsertService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WeatherForecastWriter implements ItemWriter<ForecastBatchResult> {

    private final WeatherForecastUpsertService weatherForecastUpsertService;

    @Override
    public void write(Chunk<? extends ForecastBatchResult> chunk) {
        List<Weather> allWeathers = chunk.getItems().stream()
                .flatMap(result -> result.weathers().stream())
                .toList();

        weatherForecastUpsertService.upsert(allWeathers);
    }
}