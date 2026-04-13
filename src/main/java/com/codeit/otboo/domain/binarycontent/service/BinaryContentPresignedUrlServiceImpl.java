package com.codeit.otboo.domain.binarycontent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "otboo.storage.type", havingValue = "s3")
@RequiredArgsConstructor
public class BinaryContentPresignedUrlServiceImpl implements BinaryContentPresignedUrlService{
    private final S3Presigner presigner;
    private final String bucket;

    @Value("${otboo.storage.s3.path}")
    private String path;

    @Override
    public String createPresignedUploadUrl(UUID binaryContentId, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path + "/" + binaryContentId)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        return presigner.presignPutObject(presignRequest)
                .url()
                .toString();
    }
}
