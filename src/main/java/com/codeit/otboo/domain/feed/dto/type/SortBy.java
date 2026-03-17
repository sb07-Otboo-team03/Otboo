package com.codeit.otboo.domain.feed.dto.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortBy {
    CREATE_AT("createAt"),
    LIKE_COUNT("likeCount");

    private final String description;
}
