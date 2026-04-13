package com.codeit.otboo.domain.binarycontent.dto.response;

import java.util.UUID;

public record BinaryContentPresignedUrlResponse(
        UUID binaryContentId,
        String uploadUrl
) {
}
