package com.codeit.otboo.domain.profile.repository;

import com.codeit.otboo.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    @Query("""
        select p
        from Profile p
        join fetch p.user
        where p.location is not null
          and p.location.x is not null
          and p.location.y is not null
    """)
    List<Profile> findAllForWeatherAlert();
}
