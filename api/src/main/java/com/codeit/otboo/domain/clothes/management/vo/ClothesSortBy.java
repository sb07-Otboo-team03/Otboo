package com.codeit.otboo.domain.clothes.management.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClothesSortBy {
    CREATED_AT("createdAt");

    private final String value;
}
