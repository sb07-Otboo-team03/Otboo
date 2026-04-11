package com.codeit.otboo.global.slice.dto;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public record CursorResponse<T>(
    List<T> data,
    String nextCursor,
    UUID nextIdAfter,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    SortDirection sortDirection
) {
    public static <T> CursorResponse<T> fromSlice(Slice<T> slice, String nextCursor, UUID nextIdAfter, long totalCount, String sortBy,SortDirection sortDirection) {

        return new CursorResponse<>(
            slice.getContent(),
            nextCursor,
            nextIdAfter,
            slice.hasNext(),
            (Long)totalCount,
            sortBy,
            sortDirection
        );
    }

    public static <T> CursorResponse<T> fromList(List<T> list, String nextCursor, UUID nextIdAfter, Boolean hasNext, long totalCount, String sortBy,SortDirection sortDirection) {

        return new CursorResponse<>(
            list,
            nextCursor,
            nextIdAfter,
            hasNext,
            (Long)totalCount,
            sortBy,
            sortDirection
        );
    }
}