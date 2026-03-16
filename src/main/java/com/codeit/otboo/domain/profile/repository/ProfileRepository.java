package com.codeit.otboo.domain.profile.repository;

import com.codeit.otboo.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
}
