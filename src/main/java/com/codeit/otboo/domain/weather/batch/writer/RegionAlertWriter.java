package com.codeit.otboo.domain.weather.batch.writer;

import com.codeit.otboo.domain.weather.batch.dto.RegionAlertResult;
import com.codeit.otboo.domain.weather.service.WeatherAlertBatchService;
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
