package com.codeit.otboo.domain.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Location {
    private Double latitude;
    private Double longitude;
    private Integer x;
    private Integer y;

    @Column(name = "region_1depth_name", length = 50)
    @ColumnDefault("")
    private String region1depthName;

    @Column(name = "region_2depth_name",length = 100)
    @ColumnDefault("")
    private String region2depthName;

    @Column(name = "region_3depth_name",length = 100)
    @ColumnDefault("")
    private String region3depthName;

    @Column(name = "region_4depth_name",length = 100)
    @ColumnDefault("")
    private String region4depthName;

    @Builder
    public Location(Double latitude, Double longitude, Integer x, Integer y, String region1depthName, String region2depthName, String region3depthName, String region4depthName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.x = x;
        this.y = y;
        this.region1depthName = region1depthName;
        this.region2depthName = region2depthName;
        this.region3depthName = region3depthName;
        this.region4depthName = region4depthName;
    }
}
