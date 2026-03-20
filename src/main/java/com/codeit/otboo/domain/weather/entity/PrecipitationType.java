package com.codeit.otboo.domain.weather.entity;

public enum PrecipitationType {
    NONE(0, "없음"),
    RAIN(1, "비"),
    RAIN_SNOW(2, "비/눈"),
    SNOW(3, "눈"),
    SHOWER(4, "소나기");

    private final int code;
    private final String description;

    PrecipitationType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PrecipitationType from(int code) {
        for (PrecipitationType p : values()) {
            if (p.code == code) return p;
        }
        throw new IllegalArgumentException("Unknown PTY code: " + code);
    }

    public String getDescription() {
        return description;
    }
}
