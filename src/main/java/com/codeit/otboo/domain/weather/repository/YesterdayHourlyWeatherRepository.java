package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface YesterdayHourlyWeatherRepository extends JpaRepository<YesterdayHourlyWeather, UUID> {
    Optional<YesterdayHourlyWeather> findByDateAndHour(LocalDate date, LocalTime hour);
}
