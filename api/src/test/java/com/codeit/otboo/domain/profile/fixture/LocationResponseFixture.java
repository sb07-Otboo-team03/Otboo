package com.codeit.otboo.domain.profile.fixture;

import com.codeit.otboo.domain.profile.dto.response.LocationResponse;
import com.codeit.otboo.domain.profile.entity.Location;

import java.util.List;

public class LocationResponseFixture {
    public static LocationResponse create() {
        List<String> locationNames = List.of("서울특별시", "강남구", "역삼동");

        Location location = Location.builder()
                .x(1)
                .y(2)
                .latitude(37.5)
                .longitude(126.9)
                .region1depthName(locationNames.get(0))
                .region2depthName(locationNames.get(1))
                .region3depthName(locationNames.get(2))
                .region4depthName("")
                .build();

        return new LocationResponse(
                location.getLatitude(),
                location.getLongitude(),
                location.getX(),
                location.getY(),
                locationNames
        );
    }
}
