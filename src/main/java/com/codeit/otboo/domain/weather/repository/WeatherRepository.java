package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("""
        select w
        from Weather w
        where w.forecastedAt = :forecastedAt
          and w.x = :x
          and w.y = :y
          and w.forecastAt between :start and :end
        order by w.forecastAt asc
    """)
    List<Weather> findWeatherForAlertByRegion(
            @Param("forecastedAt") LocalDateTime forecastedAt,
            @Param("x") Integer x,
            @Param("y") Integer y,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Modifying // 변경이 일어나는 쿼리 실행 시 사용
    @Query("""
        delete from Weather w
        where w.forecastedAt >= :start
          and w.forecastedAt < :end
    """)
    void deleteByForecastedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
