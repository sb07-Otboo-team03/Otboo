package com.codeit.otboo.domain.weather.entity;

public enum WindAsWord {
    WEAK, // ~ 4m/s 미만
    MODERATE, // ~ 9m/s 미만
    STRONG; // 9m/s 이상

    public static WindAsWord from (double windSpeed) {
        if (windSpeed < 4.0) return WEAK;
        if (windSpeed < 9.0) return MODERATE;
        return STRONG;
    }

}
