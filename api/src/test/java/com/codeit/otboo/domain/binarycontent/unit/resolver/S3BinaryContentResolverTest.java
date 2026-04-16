package com.codeit.otboo.domain.binarycontent.unit.resolver;

import com.codeit.otboo.domain.binarycontent.resolver.s3.S3BinaryContentUrlResolver;
import com.codeit.otboo.global.properties.StoragePathProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class S3BinaryContentResolverTest {
    @Mock
    private S3Presigner presigner;

    private static final String BUCKET = "test-bucket";
    private static final String PATH = "binary";

    private S3BinaryContentUrlResolver resolver;

    @BeforeEach
    void setUp() {
        StoragePathProperties storagePathProperties = new StoragePathProperties(
                new StoragePathProperties.S3(PATH));
        resolver = new S3BinaryContentUrlResolver(presigner, BUCKET, storagePathProperties);
    }

    @Nested
    @DisplayName("이미지 url 반환")
    class GetImageUrl {
        @Test
        @DisplayName("성공: 유효한 id 가 들어오면 presigned URL 을 생성하여 반환한다")
        void success_get_image_url() throws Exception{
            // given
            String path = "binary/";
            ReflectionTestUtils.setField(resolver, "path", path);

            UUID binaryContentId = UUID.randomUUID();
            String expectedUrl = "https://presigned-url.com";

            PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);

            given(presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                    .willReturn(presigned);

            given(presigned.url())
                    .willReturn(new URL(expectedUrl));

            // when
            String result = resolver.resolve(binaryContentId);

            // then
            assertThat(result).isEqualTo(expectedUrl);
        }
    }
}
