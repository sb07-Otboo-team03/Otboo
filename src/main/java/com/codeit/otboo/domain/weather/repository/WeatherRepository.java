package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WeatherRepository extends JpaRepository<Weather, UUID> {
}
