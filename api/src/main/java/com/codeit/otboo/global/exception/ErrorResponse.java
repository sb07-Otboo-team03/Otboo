package com.codeit.otboo.global.exception;

import lombok.Builder;

import java.util.Map;

@Builder
public record ErrorResponse(
        String exceptionName,
        String message,
        Map<String, String> details
) {
}
