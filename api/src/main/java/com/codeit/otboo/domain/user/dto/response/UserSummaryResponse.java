package com.codeit.otboo.domain.user.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserSummaryResponse(
   UUID userId,
   String name,
   String profileImageUrl
) {
}
