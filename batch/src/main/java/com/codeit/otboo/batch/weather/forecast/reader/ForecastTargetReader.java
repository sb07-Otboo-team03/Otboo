package com.codeit.otboo.batch.weather.forecast.reader;

import com.codeit.otboo.batch.weather.forecast.model.ForecastTarget;
import com.codeit.otboo.domain.weather.repository.LocationNameMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
public class ForecastTargetReader implements ItemReader<ForecastTarget> {

    private final LocationNameMapRepository locationNameMapRepository;

    private Iterator<ForecastTarget> iterator;

    @Override
    public ForecastTarget read() {
        if (iterator == null) {
            List<ForecastTarget> targets = locationNameMapRepository.findDistinctCoordinates().stream()
                    .map(p -> new ForecastTarget(p.getX(), p.getY()))
                    .toList();

            iterator = targets.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}