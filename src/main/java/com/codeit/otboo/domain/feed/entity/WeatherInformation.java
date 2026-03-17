package com.codeit.otboo.domain.feed.entity;

import com.codeit.otboo.domain.weather.entity.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.SkyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeatherInformation {

    @Column(name = "feed_weather_id", nullable = false)
    private UUID weatherId;

    /**
     * Allowed values
     * "CLEAR""MOSTLY_CLOUDY""CLOUDY"
     */
    @Column(name = "feed_sky_status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private SkyStatus skyStatus;

    /**
     * Allowed values
     * "NONE""RAIN""RAIN_SNOW""SNOW""SHOWER"
     */
    @Column(name = "feed_precipitation_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PrecipitationType precipitationType;

    @Column(name = "feed_precipitation_amount", nullable = false)
    private Double precipitationAmount;

    @Column(name = "feed_precipitation_probability", nullable = false)
    private Double precipitationProbability;

    @Column(name = "feed_temperature_current", nullable = false)
    private Double temperatureCurrent;

    @Column(name = "feed_temperature_compared_to_day_before", nullable = false)
    private Double temperatureComparedToDayBefore;

    @Column(name = "feed_temperature_max")
    private Double temperatureMax;

    @Column(name = "feed_temperature_min")
    private Double temperatureMin;
}