package com.codeit.otboo.domain.binarycontent.presignedurl.mock;

import com.codeit.otboo.domain.binarycontent.presignedurl.BinaryContentPresignedUrlService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnProperty(name = "otboo.storage.type", havingValue = "mock")
public class MockBinaryContentPresignedUrlService implements BinaryContentPresignedUrlService {
    @Override
    public String createPresignedUploadUrl(UUID binaryContentId, String contentType) {
        return "";
    }
}
