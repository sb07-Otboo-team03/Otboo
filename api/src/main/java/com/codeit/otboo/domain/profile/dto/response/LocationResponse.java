package com.codeit.otboo.domain.profile.dto.response;

import java.util.List;

public record LocationResponse(
        Double latitude,
        Double longitude,
        Integer x,
        Integer y,
        List<String> locationNames
) {
}
