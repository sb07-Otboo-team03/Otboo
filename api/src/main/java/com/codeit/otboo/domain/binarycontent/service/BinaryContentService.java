package com.codeit.otboo.domain.binarycontent.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentPresignedUrlRequest;
import com.codeit.otboo.domain.binarycontent.dto.response.BinaryContentPresignedUrlResponse;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BinaryContentService {

    BinaryContentPresignedUrlResponse getPresignedUrl(BinaryContentPresignedUrlRequest request);
    void completeUpload(UUID binaryContentId);
    void delete(UUID binaryContentId);
    BinaryContent getById(UUID id);
    void deleteAllStaleProcessingBinaryContents(LocalDateTime cutoffTime);
}
