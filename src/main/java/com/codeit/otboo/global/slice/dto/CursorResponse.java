package com.codeit.otboo.global.slice.dto;

import com.codeit.otboo.domain.directmessage.dto.CursorRequest;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import java.time.LocalDateTime;
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
    public static <T> CursorResponse<T> fromSlice(Slice<T> slice, String nextCursor, UUID nextIdAfter, String sortBy,SortDirection sortDirection) {

        return new CursorResponse<>(
            slice.getContent(),
            nextCursor,
            nextIdAfter,
            slice.hasNext(),
            null,
            sortBy,
            sortDirection
        );
    }
}