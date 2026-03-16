package com.codeit.otboo.global.slice.dto;

import java.util.List;
import java.util.UUID;

public record PageResponse<T>(
    List<T> data,
    Object nextCursor,
    UUID nextIdAfter,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    SortDirection sortDirection
) {

}