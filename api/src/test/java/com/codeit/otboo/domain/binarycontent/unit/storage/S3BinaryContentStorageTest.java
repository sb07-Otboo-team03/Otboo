package com.codeit.otboo.domain.binarycontent.unit.storage;

import com.codeit.otboo.domain.binarycontent.storage.s3.S3BinaryContentStorage;
import com.codeit.otboo.global.properties.StoragePathProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class S3BinaryContentStorageTest {
    private static final String BUCKET = "test-bucket";
    private static final String PATH = "binary";

    @Mock
    private S3Client s3Client;

    private S3BinaryContentStorage s3BinaryContentStorage;

    @BeforeEach
    void setUp() {
        StoragePathProperties storagePathProperties = new StoragePathProperties(
                new StoragePathProperties.S3(PATH));
        s3BinaryContentStorage = new S3BinaryContentStorage(s3Client, BUCKET, storagePathProperties);
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
            InputStream inputStream = s3BinaryContentStorage.get(binaryContentId);

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
            Resource resource = s3BinaryContentStorage.download(binaryContentId);

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
            s3BinaryContentStorage.delete(binaryContentId);

            // then
            then(s3Client).should().deleteObject(any(DeleteObjectRequest.class));
        }
    }

    @Nested
    @DisplayName("대상 바이너리 전체 삭제")
    class DeleteAllBinaryContent {
        @Test
        @DisplayName("성공: binaryIds 가 null 이면 메소드가 바로 종료된다")
        void success_delete_all_with_null() {
            // given
            List<UUID> binaryIds = null;

            // when
            s3BinaryContentStorage.deleteAll(binaryIds);

            // then
            then(s3Client).should(never()).deleteObjects(any(DeleteObjectsRequest.class));
        }

        @Test
        @DisplayName("성공: binaryIds가 빈 리스트이면 메소드가 바로 종료된다")
        void success_delete_all_with_empty_list() {
            // given
            List<UUID> binaryIds = List.of();

            // when
            s3BinaryContentStorage.deleteAll(binaryIds);

            // then
            then(s3Client).should(never()).deleteObjects(any(DeleteObjectsRequest.class));
        }

        @Test
        @DisplayName("성공: binaryIds 가 존재하면 DeleteObjectsRequest 를 만들어 S3 삭제를 요청한다")
        void success_delete_all() {
            // given
            UUID binaryId1 = UUID.randomUUID();
            UUID binaryId2 = UUID.randomUUID();
            List<UUID> binaryIds = List.of(binaryId1, binaryId2);

            // when
            s3BinaryContentStorage.deleteAll(binaryIds);

            // then
            ArgumentCaptor<DeleteObjectsRequest> captor =
                    ArgumentCaptor.forClass(DeleteObjectsRequest.class);

            then(s3Client).should(times(1)).deleteObjects(captor.capture());

            DeleteObjectsRequest deleteObjectsRequest = captor.getValue();
            Delete deleteRequest = deleteObjectsRequest.delete();
            List<ObjectIdentifier> targetList = deleteRequest.objects();

            assertThat(deleteObjectsRequest.bucket()).isEqualTo(BUCKET);
            assertThat(deleteRequest).isNotNull();
            assertThat(targetList).hasSize(binaryIds.size());
            assertThat(targetList.get(0).key()).isEqualTo(PATH + "/" + binaryId1);
            assertThat(targetList.get(1).key()).isEqualTo(PATH + "/" + binaryId2);
        }
    }
}
