package com.codeit.otboo.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortBy {
    CREATED_AT("createdAt"),
    EMAIL("email");

    private final String description;
}
