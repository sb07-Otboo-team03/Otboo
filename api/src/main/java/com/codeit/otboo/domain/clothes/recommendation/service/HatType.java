package com.codeit.otboo.domain.clothes.recommendation.service;

import java.util.Arrays;
import java.util.List;

public enum HatType {

    BEANIE(List.of("비니", "털모자", "니트모자")),
    CAP(List.of("캡", "볼캡", "야구모자")),
    BUCKET_HAT(List.of("버킷햇", "벙거지")),
    SUNHAT(List.of("밀짚모자", "썬햇", "챙모자")),
    BERET(List.of("베레모")),
    EARMUFFS(List.of("귀마개", "이어머프"));

    private final List<String> keywords;

    HatType(List<String> keywords) {
        this.keywords = keywords;
    }

    public static HatType from(String value) {
        if (value == null) return null;

        String normalized = value.replaceAll("\\s+", "");

        return Arrays.stream(values())
                .filter(type ->
                        type.keywords.stream().anyMatch(normalized::contains)
                ).findFirst().orElse(null);
    }
}