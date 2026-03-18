package com.codeit.otboo.domain.feed.dto.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortBy {
    CREATED_AT("createdAt"),
    LIKE_COUNT("likeCount");

    private final String description;
}
