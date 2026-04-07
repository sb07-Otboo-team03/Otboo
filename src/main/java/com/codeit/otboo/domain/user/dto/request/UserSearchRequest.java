package com.codeit.otboo.domain.user.dto.request;

import com.codeit.otboo.domain.feed.dto.type.SortBy;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.global.slice.dto.SortDirection;
import jakarta.validation.constraints.Min;

import java.util.Objects;
import java.util.UUID;

public record UserSearchRequest(
        String cursor,
        UUID idAfter,
        @Min(1)
        Integer limit,
        String sortBy,
        SortDirection sortDirection,
        String emailLike,
        Role roleEqual,
        Boolean locked
) {
    public UserSearchRequest {
        limit = Objects.requireNonNullElse(limit, 20);
        sortBy = Objects.requireNonNullElse(sortBy, SortBy.CREATED_AT.name());
        sortDirection = Objects.requireNonNullElse(sortDirection, SortDirection.DESCENDING);
    }
}
