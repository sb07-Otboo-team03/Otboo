package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.YesterdayHourlyWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface YesterdayHourlyWeatherRepository extends JpaRepository<YesterdayHourlyWeather, UUID> {
    Optional<YesterdayHourlyWeather> findByDateAndHour(LocalDate date, LocalTime hour);

    @Query("""
    select y
    from YesterdayHourlyWeather y
    where y.date = :date
      and y.x = :x
      and y.y = :y
      and y.hour between :start and :end
    order by y.hour asc
""")
    List<YesterdayHourlyWeather> findYesterdayWeatherForAlertByRegion(
            @Param("date") LocalDate date,
            @Param("x") Integer x,
            @Param("y") Integer y,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end
    );

    Optional<YesterdayHourlyWeather> findByXAndYAndDateAndHour(Integer x, Integer y, LocalDate date, LocalTime hour);
}
