package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeatherRepository extends JpaRepository<Weather, UUID> {

    Optional<Weather> findByForecastedAtAndForecastAtAndXAndY(LocalDateTime forecastedAt, LocalDateTime forecastAt, Integer x, Integer y);

    List<Weather> findByXAndYAndForecastAtBetween(int x, int y, LocalDateTime start, LocalDateTime end);
}
