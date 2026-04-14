package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherUpsertService {

    private final WeatherRepository weatherRepository;

    @Transactional
    public void upsert(List<Weather> weathers) {
        List<Weather> weatherList = new ArrayList<>();

        for (Weather weather : weathers) {
            Weather savedWeather = weatherRepository.findByForecastedAtAndForecastAtAndXAndY(
                    weather.getForecastedAt(),
                    weather.getForecastAt(),
                    weather.getX(),
                    weather.getY()
            ).orElse(null);

            if (savedWeather != null) {
                savedWeather.update(weather);
                weatherList.add(savedWeather);
            } else {
                weatherList.add(weather);
            }
        }

        weatherList.sort(
                Comparator.comparing(Weather::getForecastAt)
                        .thenComparing(Weather::getForecastedAt)
        );

        weatherRepository.saveAll(weatherList);
    }
}
