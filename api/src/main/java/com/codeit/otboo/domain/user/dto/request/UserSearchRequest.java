package com.codeit.otboo.domain.user.dto.request;


import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.global.slice.dto.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.util.Objects;
import java.util.UUID;

public record UserSearchRequest(
        @Schema(description = "커서 값")
        String cursor,
        @Schema(description ="보조 커서(생성일자), 동일 정렬 값 내 순서 보장용", example = "d25544bd-416c-4322-bcf0-9fcd98552e1d")
        UUID idAfter,
        @Min(1)
        @Schema(description = "커서 페이지 크기", example = "20")
        Integer limit,
        @Schema(description = "정렬 기준", allowableValues = {"email", "createdAt"}, defaultValue = "createdAt")
        String sortBy,
        @Schema(description = "정렬 방향", defaultValue = "DESCENDING")
        SortDirection sortDirection,
        @Schema(description = "이메일 검색", example = "emailLike")
        String emailLike,

        @Schema(description = "권한 필터")
        Role roleEqual,

        @Schema(description = "잠금 여부")
        Boolean locked
) {
    public UserSearchRequest {
        limit = Objects.requireNonNullElse(limit, 20);
        sortBy = Objects.requireNonNullElse(sortBy, SortBy.CREATED_AT.name());
        sortDirection = Objects.requireNonNullElse(sortDirection, SortDirection.DESCENDING);
    }
}