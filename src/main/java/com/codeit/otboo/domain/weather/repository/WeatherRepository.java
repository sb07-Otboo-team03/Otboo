package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeatherRepository extends JpaRepository<Weather, UUID> {

    Optional<Weather> findByForecastedAtAndForecastAtAndXAndY(LocalDateTime forecastedAt, LocalDateTime forecastAt, Integer x, Integer y);

    List<Weather> findByXAndYAndForecastAtBetween(int x, int y, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT w
        FROM Weather w
        WHERE w.forecastedAt = :forecastedAt
          AND w.forecastAt >= :start
          AND w.forecastAt < :end
    """)
    List<Weather> findYesterdayWeather(
            @Param("forecastedAt") LocalDateTime forecastedAt,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    void deleteByForecastedAtBefore(LocalDateTime forecastedAtBefore);
}
