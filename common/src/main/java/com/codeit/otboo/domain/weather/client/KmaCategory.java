package com.codeit.otboo.domain.weather.client;

import java.util.Arrays;
import java.util.Optional;

public enum KmaCategory {
    POP, // 강수 확률
    PCP, // 1시간 강수량
    PTY, // 강수 형태
    REH, // 습도
    SKY, // 하늘 상태
    TMP, // 1시간 기온
    TMX, // 일 최고 기온
    TMN, // 일 최저 기온
    WSD; // 풍속

    public static Optional<KmaCategory> from(String value) {
        return Arrays.stream(values())
                .filter(category -> category.name().equals(value))
                .findFirst();
    }
}
