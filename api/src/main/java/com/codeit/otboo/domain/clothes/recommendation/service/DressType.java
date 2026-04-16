package com.codeit.otboo.domain.clothes.recommendation.service;

import java.util.Arrays;
import java.util.List;

public enum DressType {

    SHORT_SLEEVE(List.of("반팔")),
    LONG_SLEEVE(List.of("긴팔")),
    SLEEVELESS(List.of("나시", "뷔스티에")),
    KNIT(List.of("니트", "모직", "기모"));

    private final List<String> keywords;

    DressType(List<String> keywords) {
        this.keywords = keywords;
    }

    public static DressType from(String value) {
        if (value == null) return null;

        String normalized = value.replaceAll("\\s+", "");
        return Arrays.stream(values())
                .filter(type ->
                        type.keywords.stream().anyMatch(normalized::contains)
                ).findFirst().orElse(null);
    }

}
