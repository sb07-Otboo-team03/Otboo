package com.codeit.otboo.batch.weather.alert.reader;

import com.codeit.otboo.batch.weather.alert.model.RegionAlertTarget;
import com.codeit.otboo.batch.weather.alert.service.WeatherAlertBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegionAlertTargetReader {

    private final WeatherAlertBatchService weatherAlertBatchService;

    public ListItemReader<RegionAlertTarget> reader() {
        List<RegionAlertTarget> items = weatherAlertBatchService.findAlertTargetsByRegion();
        return new ListItemReader<>(items);
    }
}
