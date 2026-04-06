package com.codeit.otboo.domain.user.dto.request;

import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.global.slice.dto.SortDirection;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserSearchCondition(
        String cursor,
        UUID idAfter,
        Integer limit,
        String sortBy,
        SortDirection sortDirection,
        String emailLike,
        Role roleEqual,
        Boolean locked
) {
    public static UserSearchCondition from(UserSearchRequest request){
        return new UserSearchCondition(
                request.cursor(),
                request.idAfter(),
                request.limit(),
                request.sortBy(),
                request.sortDirection(),
                request.emailLike(),
                request.roleEqual(),
                request.locked()
        );
    }
}
