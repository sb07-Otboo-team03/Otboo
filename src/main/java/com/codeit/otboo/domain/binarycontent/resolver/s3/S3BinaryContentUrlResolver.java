package com.codeit.otboo.domain.binarycontent.resolver.s3;

import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "otboo.storage.type", havingValue = "s3")
@RequiredArgsConstructor
public class S3BinaryContentUrlResolver implements BinaryContentUrlResolver {
    private final S3Presigner presigner;
    private final String bucket;

    @Override
    public String resolve(UUID binaryContentId) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key("binary/" + binaryContentId)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(objectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}