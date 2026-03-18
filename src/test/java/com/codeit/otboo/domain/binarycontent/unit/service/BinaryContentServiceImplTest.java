package com.codeit.otboo.domain.binarycontent.unit.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateReq;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.repository.BinaryContentRepository;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentServiceImpl;
import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import com.codeit.otboo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class BinaryContentServiceImplTest {
    @Mock
    BinaryContentRepository binaryContentRepository;

    @Mock
    BinaryContentStorage binaryContentStorage;

    @InjectMocks
    BinaryContentServiceImpl binaryContentService;

    @Nested
    @DisplayName("바이너리 컨텐츠 업로드")
    class UploadBinaryContent {
        @Test
        @DisplayName("성공: 유효한 파일 정보가 들어오면 메타데이터를 DB에 저장하고, 생성된 ID를 파일이름으로 하여 데이터를 저장한다")
        void success_upload() {
            // given
            byte[] data = "test".getBytes();
            BinaryContentCreateReq req = new BinaryContentCreateReq(
                    data, "test_file", "image/png", 30L);
            BinaryContent binaryContent = BinaryContentFixture.create(req);
            given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(binaryContent);

            // when
            BinaryContent result = binaryContentService.upload(req);

            // then
            assertThat(result).isEqualTo(binaryContent);
            then(binaryContentRepository).should(times(1))
                    .save(any(BinaryContent.class));
            then(binaryContentStorage).should(times(1))
                    .put(eq(binaryContent.getId()), eq(data));
        }
    }

    @Nested
    @DisplayName("바이너리 컨텐츠 다운로드")
    class DownloadBinaryContent {
        @Test
        @DisplayName("성공: 유효한 id 값이 들어오면 다운로드에 성공한다")
        void success_download() {
            // given
            BinaryContent binaryContent = BinaryContentFixture.create();
            Resource resource = mock(Resource.class);
            given(binaryContentRepository.findById(any(UUID.class)))
                    .willReturn(Optional.of(binaryContent));
            given(binaryContentStorage.download(any(UUID.class)))
                    .willReturn(resource);

            // when
            Resource result = binaryContentService.download(binaryContent.getId());

            // then
            assertThat(result).isEqualTo(resource);
            then(binaryContentRepository).should(times(1))
                    .findById(any(UUID.class));
            then(binaryContentStorage).should(times(1))
                    .download(any(UUID.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 id 값이 들어오면 예외가 발생한다")
        void fail_download_binary_not_found() {
            // given
            UUID binaryContentId = UUID.randomUUID();
            given(binaryContentRepository.findById(any(UUID.class)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> binaryContentService.download(binaryContentId))
                    .isInstanceOf(BinaryContentNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BINARY_CONTENT_NOT_FOUNT);
            then(binaryContentStorage).should(never())
                    .download(any());
        }
    }

    @Nested
    @DisplayName("바이너리 컨텐츠 메타데이터 읽기")
    class ReadBinaryContent {
        @Test
        @DisplayName("성공: 유효한 id 값이 들어오면 메타데이터 가져오기에 성공한다")
        void success_get_info() {
            // given
            BinaryContent binaryContent = BinaryContentFixture.create();
            given(binaryContentRepository.findById(any(UUID.class)))
                    .willReturn(Optional.of(binaryContent));

            // when
            BinaryContent result = binaryContentService.getInfo(binaryContent.getId());

            // then
            assertThat(result).isEqualTo(binaryContent);
            then(binaryContentRepository).should(times(1))
                    .findById(binaryContent.getId());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 id 값이 들어오면 예외가 발생한다")
        void fail_get_info_binary_not_found() {
            // given
            UUID binaryContentId = UUID.randomUUID();
            given(binaryContentRepository.findById(any(UUID.class)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> binaryContentService.getInfo(binaryContentId))
                    .isInstanceOf(BinaryContentNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BINARY_CONTENT_NOT_FOUNT);
        }
    }

    @Nested
    @DisplayName("바이너리 컨텐츠 삭제")
    class DeleteBinaryContent {
        @Test
        @DisplayName("성공: 유효한 id 값이 들어오면 바이너리 컨텐츠가 삭제된다")
        void success_delete() {
            // given
            BinaryContent binaryContent = BinaryContentFixture.create();
            given(binaryContentRepository.findById(any(UUID.class)))
                    .willReturn(Optional.of(binaryContent));

            // when
            binaryContentService.delete(binaryContent.getId());

            // then
            then(binaryContentRepository).should(times(1))
                    .findById(binaryContent.getId());
            then(binaryContentRepository).should(times(1))
                    .delete(binaryContent);
            then(binaryContentStorage).should(times(1))
                    .delete(binaryContent.getId());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 id 값이 들어오면 예외가 발생한다")
        void fail_delete_binary_not_found() {
            // given
            UUID binaryContentId = UUID.randomUUID();
            given(binaryContentRepository.findById(any(UUID.class)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> binaryContentService.delete(binaryContentId))
                    .isInstanceOf(BinaryContentNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BINARY_CONTENT_NOT_FOUNT);
            then(binaryContentStorage).should(never())
                    .delete(any());
            then(binaryContentRepository).should(never())
                    .delete(any());
        }
    }
}
