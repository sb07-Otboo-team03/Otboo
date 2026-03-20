package com.codeit.otboo.domain.weather.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(callSuper = true)
public class Weather extends BaseUpdatableEntity {

    @Column(nullable = false)
    private LocalDateTime forecastedAt;

    @Column(nullable = false)
    private LocalDateTime forecastAt;

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
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
    private SkyStatus SkyStatus; // 하늘 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrecipitationType precipitationType; // 강수 타입

    @Column(nullable = false)
    private Double precipitationAmount; // 강수량

    @Column(nullable = false)
    private Double precipitationProbability; // 강수 확률

    @Column(nullable = false)
    private Double humidityCurrent; // 현재 습도


    public void update(LocalDateTime forecastedAt,
                       LocalDateTime forecastAt,
                       Double temperatureCurrent,
                       Double temperatureMax,
                       Double temperatureMin,
                       Double windSpeed,
                       WindAsWord windAsWord,
                       SkyStatus skyStatus,
                       PrecipitationType precipitationType,
                       Double precipitationAmount,
                       Double precipitationProbability,
                       Double humidityCurrent
    ) {
        if (forecastedAt != null && !forecastedAt.equals(this.forecastedAt)) {
            this.forecastedAt = forecastedAt;
        }
        if (forecastAt != null && !forecastAt.equals(this.forecastAt)) {
            this.forecastAt = forecastAt;
        }
        if (temperatureCurrent != null && !temperatureCurrent.equals(this.temperatureCurrent)) {
            this.temperatureCurrent = temperatureCurrent;
        }
        if (temperatureMax != null && !temperatureMax.equals(this.temperatureMax)) {
            this.temperatureMax = temperatureMax;
        }
        if (temperatureMin != null && !temperatureMin.equals(this.temperatureMin)) {
            this.temperatureMin = temperatureMin;
        }
        if (windSpeed != null && !windSpeed.equals(this.windSpeed)) {
            this.windSpeed = windSpeed;
        }
        if (windAsWord != null && !windAsWord.equals(this.windAsWord)) {
            this.windAsWord = windAsWord;
        }
        if (skyStatus != null && !skyStatus.equals(this.SkyStatus)) {
            this.SkyStatus = skyStatus;
        }
        if (precipitationType != null && !precipitationType.equals(this.precipitationType)) {
            this.precipitationType = precipitationType;
        }
        if (precipitationAmount != null && !precipitationAmount.equals(this.precipitationAmount)) {
            this.precipitationAmount = precipitationAmount;
        }
        if (precipitationProbability != null && !precipitationProbability.equals(this.precipitationProbability)) {
            this.precipitationProbability = precipitationProbability;
        }
        if (humidityCurrent != null && !humidityCurrent.equals(this.humidityCurrent)) {
            this.humidityCurrent = humidityCurrent;
        }
    }


    public Weather(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
