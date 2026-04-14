package com.codeit.otboo.domain.weather.entity;

public enum SkyStatus {
    CLEAR(1, "맑음"),
    MOSTLY_CLOUDY(3, "구름 많음"),
    CLOUDY(4, "흐림");

    private final int code;
    private final String description;

    SkyStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SkyStatus from(int code) {
        for (SkyStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown SKY code: " + code);
    }

    public String getDescription() {
        return description;
    }
}
