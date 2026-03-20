package com.codeit.otboo.domain.weather.entity;

import com.codeit.otboo.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 어제 날짜의 온도, 습도 비교용 데이터
 */
@Entity
@Table(name = "yesterday_hourly_weather")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class YesterdayHourlyWeather extends BaseEntity {

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
    private Integer y;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime hour;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Double humidity;
}
