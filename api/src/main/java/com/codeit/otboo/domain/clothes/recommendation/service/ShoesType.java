package com.codeit.otboo.domain.clothes.recommendation.service;

import java.util.Arrays;
import java.util.List;

public enum ShoesType {

    SNEAKERS(List.of("운동화", "러닝화", "스니커즈")),
    SANDALS(List.of("샌들")),
    SLIPPERS(List.of("슬리퍼", "크록스")),
    BOOTS(List.of("워커")),
    RAIN_BOOTS(List.of("장화", "레인부츠")),
    WINTER_BOOTS(List.of("양털부츠")),
    FORMAL(List.of("구두"));

    private final List<String> keywords;

    ShoesType(List<String> keywords) {
        this.keywords = keywords;
    }

    public static ShoesType from(String value) {
        if (value == null) return null;

        String normalized = value.replaceAll("\\s+", "");

        return Arrays.stream(values())
                .filter(type ->
                        type.keywords.stream().anyMatch(normalized::contains)
                ).findFirst().orElse(null);
    }
}
