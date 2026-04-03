package com.codeit.otboo.domain.binarycontent.unit.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentCreatedEvent;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentDeletedEvent;
import com.codeit.otboo.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.codeit.otboo.domain.binarycontent.exception.FileUploadMaximumSizeException;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.repository.BinaryContentRepository;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentServiceImpl;
import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.properties.MultipartProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.unit.DataSize;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class BinaryContentServiceImplTest {
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @InjectMocks
    private BinaryContentServiceImpl binaryContentService;

    @BeforeEach
    void setUp() {
        MultipartProperties props = new MultipartProperties(DataSize.ofMegabytes(10));

        binaryContentService = new BinaryContentServiceImpl(
                eventPublisher,
                binaryContentRepository,
                props
        );
    }

    @Nested
    @DisplayName("바이너리 컨텐츠 업로드")
    class UploadBinaryContent {
        @Test
        @DisplayName("성공: 유효한 파일 정보가 들어오면 메타데이터를 DB에 저장하고, 생성된 ID를 파일이름으로 하여 데이터를 저장한다")
        void success_upload() {
            // given
            byte[] data = "test".getBytes();
            BinaryContentCreateRequest request = new BinaryContentCreateRequest(
                    data, "test_file", "image/png", 30L);
            BinaryContent binaryContent = BinaryContentFixture.create(request);
            given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(binaryContent);

            // when
            BinaryContent result = binaryContentService.upload(request);

            // then
            assertThat(result).isEqualTo(binaryContent);
            then(binaryContentRepository).should(times(1))
                    .save(any(BinaryContent.class));
            then(eventPublisher).should().publishEvent(any(BinaryContentCreatedEvent.class));
        }

        @Test
        @DisplayName("실패: 최대 용량이 넘는 파일이 업로드되면 예외가 발생한다")
        void fail_put_binary_content_exceed_max_size() {
            // given
            byte[] data = new byte[20 * 1024 * 1024]; // 20 MB
            BinaryContentCreateRequest request =
                    new BinaryContentCreateRequest(data, "test_file", "image/png", data.length);

            // when & then
            assertThatThrownBy(() -> binaryContentService.upload(request))
                    .isInstanceOf(FileUploadMaximumSizeException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FILE_UPLOAD_MAXIMUM_SIZE);

            then(binaryContentRepository).should(never())
                    .save(any(BinaryContent.class));
            then(eventPublisher).should(never())
                    .publishEvent(any(BinaryContentCreatedEvent.class));
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
            then(eventPublisher).should()
                    .publishEvent(any(BinaryContentDeletedEvent.class));
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
                    .isEqualTo(ErrorCode.BINARY_CONTENT_NOT_FOUND);
            then(binaryContentRepository).should(never())
                    .delete(any());
            then(eventPublisher).should(never()).publishEvent(any(BinaryContentDeletedEvent.class));
        }
    }
}
