package com.codeit.otboo.domain.binarycontent.unit.service;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.presignedurl.s3.S3BinaryContentPresignedUrlService;
import com.codeit.otboo.global.properties.StoragePathProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class S3BinaryContentPresignedUrlServiceTest {
    private static final String BUCKET = "test-bucket";
    private static final String PATH = "binary";

    @Mock
    private S3Presigner presigner;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    private S3BinaryContentPresignedUrlService s3BinaryContentPresignedUrlService;

    @BeforeEach
    void setUp() {
        StoragePathProperties storagePathProperties = new StoragePathProperties(
                new StoragePathProperties.S3(PATH));

        s3BinaryContentPresignedUrlService = new S3BinaryContentPresignedUrlService(
                presigner, BUCKET, storagePathProperties);
    }

    @Nested
    @DisplayName("Presigned Url 생성")
    class CreatePresignedUrl {
        @Test
        @DisplayName("성공: 유효한 파라미터가 들어올 경우 Presigned Url이 생성된다")
        void success_create_presigned_url() throws Exception {
            // given
            BinaryContent binaryContent = BinaryContentFixture.create(
                    "test.jpg", "image/jpeg", 10L);
            String expectedUrl = "https://test-bucket.s3.amazonaws.com/binary/" + binaryContent.getId();

            given(presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                    .willReturn(presignedPutObjectRequest);
            given(presignedPutObjectRequest.url())
                    .willReturn(URI.create(expectedUrl).toURL());

            // when
            String result = s3BinaryContentPresignedUrlService.createPresignedUploadUrl(
                    binaryContent.getId(), binaryContent.getType());

            // then
            assertThat(result).isEqualTo(expectedUrl);

            then(presigner).should(times(1))
                    .presignPutObject(any(PutObjectPresignRequest.class));
        }
    }

}
