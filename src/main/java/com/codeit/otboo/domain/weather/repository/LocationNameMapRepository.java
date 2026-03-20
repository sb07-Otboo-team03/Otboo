package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.LocationNameMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationNameMapRepository extends JpaRepository<LocationNameMap, UUID> {
    Optional<LocationNameMap> findByLongitudeAndLatitude(Double longitude, Double latitude);
}
