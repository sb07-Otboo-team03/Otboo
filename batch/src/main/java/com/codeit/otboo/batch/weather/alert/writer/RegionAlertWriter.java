package com.codeit.otboo.batch.weather.alert.writer;

import com.codeit.otboo.batch.weather.alert.model.RegionAlertResult;
import com.codeit.otboo.batch.weather.alert.service.WeatherAlertBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionAlertWriter implements ItemWriter<RegionAlertResult> {

    private final WeatherAlertBatchService weatherAlertBatchService;

    @Override
    public void write(Chunk<? extends RegionAlertResult> chunk) {
        for (RegionAlertResult result : chunk) {
            weatherAlertBatchService.publishWeatherEvent(result);
        }
    }
}
