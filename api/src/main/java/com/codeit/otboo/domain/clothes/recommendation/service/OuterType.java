package com.codeit.otboo.domain.clothes.recommendation.service;

import java.util.Arrays;
import java.util.List;

public enum OuterType {

    JACKET(List.of("자켓", "바람막이")),
    COAT(List.of("코트")),
    PADDING(List.of("패딩", "점퍼")),
    CARDIGAN(List.of("가디건"));

    private final List<String> keywords;

    OuterType(List<String> keywords) {
        this.keywords = keywords;
    }

    public static OuterType from(String value) {
        if(value == null) return null;

        String normalized = value.replaceAll("\\s+", "");

        return Arrays.stream(values())
                .filter(type ->
                        type.keywords.stream().anyMatch(normalized::contains)
                ).findFirst().orElse(null);
    }

}
