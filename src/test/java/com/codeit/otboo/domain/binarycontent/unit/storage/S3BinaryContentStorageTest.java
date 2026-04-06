package com.codeit.otboo.domain.binarycontent.unit.storage;

import com.codeit.otboo.domain.binarycontent.storage.s3.S3BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class S3BinaryContentStorageTest {
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3BinaryContentStorage storage;

    @Nested
    @DisplayName("바이너리 데이터 저장")
    class PutBinaryContent {
        @Test
        @DisplayName("성공: id와 data 가 들어오면 해당 id값으로 파일이 만들어진다")
        void success_put_binary_content() throws Exception {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] data = "test".getBytes();
            given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .willReturn(PutObjectResponse.builder().build());

            // when
            UUID savedId = storage.put(binaryContentId, data, "image/jpeg");

            // then
            assertThat(savedId).isEqualTo(binaryContentId);
            then(s3Client).should().putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }

    @Nested
    @DisplayName("바이너리 데이터 조회")
    class GetBinaryContent {
        @Test
        @DisplayName("성공: 존재하는 파일 조회")
        void success_get_binary_content() throws Exception {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] data = "test".getBytes();
            ResponseInputStream<GetObjectResponse> responseStream =
                new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        new ByteArrayInputStream(data)
                );
            given(s3Client.getObject(any(GetObjectRequest.class))).willReturn(responseStream);

            // when
            InputStream inputStream = storage.get(binaryContentId);

            // then
            assertThat(inputStream.readAllBytes()).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("바이너리 데이터 다운로드")
    class DownloadBinaryContent {
        @Test
        @DisplayName("존재하는 파일의 id가 들어올 경우 Resource를 반환한다")
        void success_download_binary_content() throws Exception {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] data = "test".getBytes();
            ResponseInputStream<GetObjectResponse> responseStream =
                    new ResponseInputStream<>(
                            GetObjectResponse.builder().build(),
                            new ByteArrayInputStream(data)
                    );
            given(s3Client.getObject(any(GetObjectRequest.class)))
                    .willReturn(responseStream);

            // when
            Resource resource = storage.download(binaryContentId);

            // then
            assertThat(resource).isNotNull();
            assertThat(resource.getInputStream().readAllBytes()).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("바이너리 데이터 다운로드")
    class DeleteBinaryContent {
        @Test
        @DisplayName("성공: 존재하는 파일의 id가 들어오면 S3 deleteObject가 호출된다")
        void success_delete_binary_content() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .willReturn(DeleteObjectResponse.builder().build());

            // when
            storage.delete(binaryContentId);

            // then
            then(s3Client).should().deleteObject(any(DeleteObjectRequest.class));
        }
    }
}
