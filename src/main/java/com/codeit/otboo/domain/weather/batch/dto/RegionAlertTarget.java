package com.codeit.otboo.domain.weather.batch.dto;

import com.codeit.otboo.domain.profile.entity.Profile;

import java.util.List;

public record RegionAlertTarget(
        Integer x,
        Integer y,
        List<Profile> profiles
) {
}
