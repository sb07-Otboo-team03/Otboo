package com.codeit.otboo.batch.weather.alert.processor;

import com.codeit.otboo.batch.weather.alert.model.RegionAlertResult;
import com.codeit.otboo.batch.weather.alert.model.RegionAlertTarget;
import com.codeit.otboo.batch.weather.alert.service.WeatherAlertBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionAlertProcessor implements ItemProcessor<RegionAlertTarget, RegionAlertResult> {

    private final WeatherAlertBatchService weatherAlertBatchService;

    @Override
    public RegionAlertResult process(RegionAlertTarget item) {
        RegionAlertResult result = weatherAlertBatchService.buildRegionAlertResult(item);
        return result.isEmpty() ? null : result;
    }
}
