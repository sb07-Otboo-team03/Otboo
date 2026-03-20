package com.codeit.otboo.domain.weather.entity;

import com.codeit.otboo.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개인의 위도, 경도 값과 지역명 매핑 테이블
 */
@Entity
@Table(name = "location_name_map",
       uniqueConstraints = @UniqueConstraint(columnNames = {"x", "y"}))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class LocationNameMap extends BaseEntity {

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
    private Integer y;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "region_1depth_name", length = 50)
    private String region1depthName;

    @Column(name = "region_2depth_name",length = 100)
    private String region2depthName;

    @Column(name = "region_3depth_name",length = 100)
    private String region3depthName;

    @Column(name = "region_4depth_name",length = 100)
    private String region4depthName;
}
