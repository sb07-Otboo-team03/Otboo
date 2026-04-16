package com.codeit.otboo.domain.clothes.recommendation.service;

import java.util.Arrays;
import java.util.List;

public enum TopType {

    SHORT_SLEEVE(List.of("반팔")),
    LONG_SLEEVE(List.of("긴팔", "긴소매", "롱슬리브", "롱탑", "LONG SLEEVE")),
    KNIT(List.of("니트", "카디건", "가디건")),
    SWEATSHIRT(List.of("맨투맨", "스웨트")),
    HOODIE(List.of("후드")),
    TURTLENECK(List.of("목폴라", "목티")),
    SLEEVELESS(List.of("나시", "민소매"));

    private final List<String> keywords;

    TopType(List<String> keywords) {
        this.keywords = keywords;
    }

    public static TopType from(String value) {
        if (value == null) return null;

        String normalized = value.replaceAll("\\s+", "");

        return Arrays.stream(values())
                .filter(type ->
                        type.keywords.stream().anyMatch(normalized::contains)
                ).findFirst().orElse(null);
    }
}
