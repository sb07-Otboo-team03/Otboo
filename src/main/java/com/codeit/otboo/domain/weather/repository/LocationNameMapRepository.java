package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.LocationNameMap;
import com.codeit.otboo.domain.weather.repository.projection.CoordinateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationNameMapRepository extends JpaRepository<LocationNameMap, UUID> {
    Optional<LocationNameMap> findByLongitudeAndLatitude(Double longitude, Double latitude);

    @Query("select distinct l.x as x, l.y as y from LocationNameMap l")
    List<CoordinateProjection> findDistinctCoordinates();
}
