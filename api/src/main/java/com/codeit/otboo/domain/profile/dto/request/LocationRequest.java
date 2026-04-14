package com.codeit.otboo.domain.profile.dto.request;

import java.util.List;

public record LocationRequest(
        Double latitude,
        Double longitude,
        Integer x,
        Integer y,
        List<String> locationNames
) {
}