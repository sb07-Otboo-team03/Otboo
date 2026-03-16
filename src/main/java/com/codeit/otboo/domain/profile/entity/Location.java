package com.codeit.otboo.domain.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Location {
    private Double latitude;
    private Double longitude;
    private Integer x;
    private Integer y;

    @Builder
    public Location(Double latitude, Double longitude, Integer x, Integer y) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.x = x;
        this.y = y;
    }
}
