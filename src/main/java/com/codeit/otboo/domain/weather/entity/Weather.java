package com.codeit.otboo.domain.weather.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "weathers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime forecastedAt;

    @Column(nullable = false)
    private LocalDateTime forecastAt;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
    private Integer y;

    @Column(nullable = false, length = 30)
    private String locationNames;

    @Column(nullable = false)
    private Double temperatureCurrent;

    private Double temperatureMax;

    private Double temperatureMin;

    @Column(nullable = false)
    private Double windSpeed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WindAsWord windAsWord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkyStatus SkyStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrecipitationType precipitationType;

    @Column(nullable = false)
    private Double precipitationAmount;

    @Column(nullable = false)
    private Double precipitationProbability;

    @Column(nullable = false)
    private Double humidityCurrent;

    public Weather(UUID id) {
        this.id = id;
    }
}
