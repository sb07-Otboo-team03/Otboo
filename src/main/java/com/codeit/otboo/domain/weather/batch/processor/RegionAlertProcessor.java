package com.codeit.otboo.domain.weather.batch.processor;

import com.codeit.otboo.domain.weather.batch.dto.RegionAlertResult;
import com.codeit.otboo.domain.weather.batch.dto.RegionAlertTarget;
import com.codeit.otboo.domain.weather.service.WeatherAlertBatchService;
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
