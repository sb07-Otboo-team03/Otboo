package com.codeit.otboo.domain.clothes.recommendation.service;

import java.util.Arrays;
import java.util.List;

public enum BottomType {

    SHORTS(List.of("반바지")),
    JEANS(List.of("청바지")),
    SLACKS(List.of("슬랙스")),
    SKIRT(List.of("치마")),
    TRAINING(List.of("트레이닝"));

    private final List<String> keywords;

    BottomType(List<String> keywords) {
        this.keywords = keywords;
    }

    public static BottomType from(String value) {
        if (value == null) return null;

        String normalized = value.replaceAll("\\s+", "");

        return Arrays.stream(values())
                .filter(type ->
                        type.keywords.stream().anyMatch(normalized::contains)
                ).findFirst().orElse(null);
    }

}