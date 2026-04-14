package com.codeit.otboo.domain.binarycontent.presignedurl;

import java.util.UUID;

public interface BinaryContentPresignedUrlService {
    String createPresignedUploadUrl(UUID binaryContentId, String contentType);
}
