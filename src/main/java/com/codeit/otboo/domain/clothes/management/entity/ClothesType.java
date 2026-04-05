package com.codeit.otboo.domain.clothes.management.entity;

public enum ClothesType {
    TOP,
    BOTTOM,
    DRESS,
    OUTER,
    UNDERWEAR,
    ACCESSORY,
    SHOES,
    SOCKS,
    HAT,
    BAG,
    SCARF,
    ETC;

    public static ClothesType fromString(String value){
        return value == null ? null : ClothesType.valueOf(value.toUpperCase());
    }
}
