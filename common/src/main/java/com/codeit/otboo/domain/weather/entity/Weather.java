package com.codeit.otboo.domain.weather.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "weather",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_weather_forecasted_forecast_xy",
                        columnNames = {"forecasted_at", "forecast_at", "x", "y"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(callSuper = true)
public class Weather extends BaseUpdatableEntity {

    @Column(nullable = false)
    private LocalDateTime forecastedAt;

    @Column(nullable = false)
    private LocalDateTime forecastAt;

    @Column(nullable = false, updatable = false)
    private Integer x;

    @Column(nullable = false, updatable = false)
    private Integer y;

    @Column(nullable = false)
    private Double temperatureCurrent; // 현재 온도

    private Double temperatureMax; // 최고 온도

    private Double temperatureMin; // 최저 온도

    @Column(nullable = false)
    private Double windSpeed; // 풍속

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WindAsWord windAsWord; // 바람 세기

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkyStatus skyStatus; // 하늘 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrecipitationType precipitationType; // 강수 타입

    @Column(nullable = false)
    private Double precipitationAmount; // 강수량

    @Column(nullable = false)
    private Double precipitationProbability; // 강수 확률

    @Column(nullable = false)
    private Double humidityCurrent; // 현재 습도


    public void update(Weather weather) {
        this.forecastedAt = updateIfChanged(this.forecastedAt, weather.forecastedAt);
        this.forecastAt = updateIfChanged(this.forecastAt, weather.forecastAt);
        this.temperatureCurrent = updateIfChanged(this.temperatureCurrent, weather.temperatureCurrent);
        this.temperatureMax = updateIfChanged(this.temperatureMax, weather.temperatureMax);
        this.temperatureMin = updateIfChanged(this.temperatureMin, weather.temperatureMin);
        this.windSpeed = updateIfChanged(this.windSpeed, weather.windSpeed);
        this.windAsWord = updateIfChanged(this.windAsWord, weather.windAsWord);
        this.skyStatus = updateIfChanged(this.skyStatus, weather.skyStatus);
        this.precipitationType = updateIfChanged(this.precipitationType, weather.precipitationType);
        this.precipitationAmount = updateIfChanged(this.precipitationAmount, weather.precipitationAmount);
        this.precipitationProbability = updateIfChanged(this.precipitationProbability, weather.precipitationProbability);
        this.humidityCurrent = updateIfChanged(this.humidityCurrent, weather.humidityCurrent);
    }

    private <T> T updateIfChanged(T current, T newValue) {
        if (newValue != null && !newValue.equals(current)) {
            return newValue;
        }
        return current;
    }
}
