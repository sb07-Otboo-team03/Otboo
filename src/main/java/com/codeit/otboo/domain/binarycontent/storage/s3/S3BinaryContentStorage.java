package com.codeit.otboo.domain.binarycontent.storage.s3;

import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "otboo.storage.type", havingValue = "s3")
@RequiredArgsConstructor
public class S3BinaryContentStorage implements BinaryContentStorage {
    private final S3Client s3Client;
    private final String bucket;

    @Value("${otboo.storage.s3.path}")
    private String path;

    private String key(UUID binaryId) {
        return path + "/" + binaryId;
    }

    @Override
    public UUID put(UUID binaryId, byte[] data) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key(binaryId))
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        return binaryId;
    }

    @Override
    public InputStream get(UUID binaryId) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key(binaryId))
                .build();

        return s3Client.getObject(request);
    }

    @Override
    public Resource download(UUID binaryId) {
        return new InputStreamResource(get(binaryId));
    }

    @Override
    public void delete(UUID binaryId) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key(binaryId))
                .build();

        s3Client.deleteObject(request);
    }
}
