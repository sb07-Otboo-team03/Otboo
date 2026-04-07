package com.codeit.otboo.domain.weather.batch.reader;

import com.codeit.otboo.domain.weather.batch.dto.RegionAlertTarget;
import com.codeit.otboo.domain.weather.service.WeatherAlertBatchService;
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
