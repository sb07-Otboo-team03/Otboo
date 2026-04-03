package com.codeit.otboo.domain.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
@Builder
public class Location {
    private Double latitude;
    private Double longitude;
    private Integer x;
    private Integer y;

    @Column(name = "region_1depth_name", length = 50)
    private String region1depthName ="";

    @Column(name = "region_2depth_name",length = 100)
    private String region2depthName = "";

    @Column(name = "region_3depth_name",length = 100)
    private String region3depthName = "";

    @Column(name = "region_4depth_name",length = 100)
    @Builder.Default
    private String region4depthName = "";

}
