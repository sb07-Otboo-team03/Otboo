package com.codeit.otboo.domain.binarycontent.unit.storage;


import com.codeit.otboo.domain.binarycontent.storage.local.LocalBinaryContentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LocalBinaryContentStorageTest {
    @TempDir
    private Path tempDir;

    private LocalBinaryContentStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 저장 경로 생성
        storage = new LocalBinaryContentStorage(tempDir);
        storage.init();
    }

    @Nested
    @DisplayName("바이너리 데이터 저장")
    class PutBinaryContent {
        @Test
        @DisplayName("성공: id와 data 가 들어오면 해당 id값으로 파일이 만들어진다")
        void success_put_binary_content() throws Exception {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] data = "test".getBytes();

            // when
            UUID savedId = storage.put(binaryContentId, data);

            // then
            Path savedFile = tempDir.resolve(binaryContentId.toString());

            assertThat(savedId).isEqualTo(binaryContentId);
            assertThat(Files.exists(savedFile)).isTrue();
            assertThat(Files.readAllBytes(savedFile)).isEqualTo(data);
        }

        @Test
        @DisplayName("실패: id값이 null이면 NullPointerException이 발생한다")
        void fail_put_binary_content_null_point_exception() {
            // given
            byte[] data = "test".getBytes();

            // when & then
            assertThatThrownBy(() -> storage.put(null, data))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("바이너리 데이터 조회")
    class GetBinaryContent {
        @Test
        @DisplayName("성공: 존재하는 파일의 id가 들어올 경우 조회할 수 있다")
        void success_get_binary_content() throws Exception {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] data = "test".getBytes();
            Files.write(tempDir.resolve(binaryContentId.toString()), data);

            // when
            InputStream inputStream = storage.get(binaryContentId);

            // then
            assertThat(inputStream).isNotNull();
            assertThat(inputStream.readAllBytes()).isEqualTo(data);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 파일을 조회하면 UncheckedIOException이 발생한다")
        void fail_get_binary_content_file_not_exist() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> storage.get(binaryContentId))
                    .isInstanceOf(UncheckedIOException.class);
        }
    }

    @Nested
    @DisplayName("바이너리 데이터 다운로드")
    class DownloadBinaryContent {
        @Test
        @DisplayName("성공: 존재하는 파일의 id가 들어올 경우 Resource를 반환한다")
        void success_download_binary_content() throws Exception {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] data = "test".getBytes();
            Files.write(tempDir.resolve(binaryContentId.toString()), data);

            // when
            Resource resource = storage.download(binaryContentId);

            // then
            assertThat(resource).isNotNull();
            assertThat(resource.getFilename()).isEqualTo(binaryContentId.toString());
            assertThat(resource.getInputStream().readAllBytes()).isEqualTo(data);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 파일 다운로드 시 UncheckedIOException 발생한다")
        void fail_download_binary_content_file_not_exist() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> storage.download(binaryContentId))
                    .isInstanceOf(UncheckedIOException.class);
        }
    }

    @Nested
    @DisplayName("바이너리 데이터 삭제")
    class DeleteBinaryContent {
        @Test
        @DisplayName("성공: 존재하는 파일의 id가 들어올 경우 파일이 삭제된다")
        void success_delete_binary_content() throws Exception {
            // given
            UUID binaryContentId = UUID.randomUUID();
            byte[] data = "test".getBytes();

            Path savedFile = tempDir.resolve(binaryContentId.toString());
            Files.write(savedFile, data);

            // when
            storage.delete(binaryContentId);

            // then
            assertThat(Files.exists(savedFile)).isFalse();
        }
    }
}
