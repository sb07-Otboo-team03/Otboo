package com.codeit.otboo.domain.binarycontent.service;

import java.util.UUID;

public interface BinaryContentPresignedUrlService {
    String createPresignedUploadUrl(UUID binaryContentId, String contentType);
}
