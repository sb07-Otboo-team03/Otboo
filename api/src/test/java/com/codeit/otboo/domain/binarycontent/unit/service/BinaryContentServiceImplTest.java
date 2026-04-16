package com.codeit.otboo.domain.binarycontent.unit.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentPresignedUrlRequest;
import com.codeit.otboo.domain.binarycontent.dto.response.BinaryContentPresignedUrlResponse;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.entity.UploadStatus;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentDeletedEvent;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentListDeletedEvent;
import com.codeit.otboo.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.codeit.otboo.domain.binarycontent.exception.FileTypeNotSupportException;
import com.codeit.otboo.domain.binarycontent.exception.FileUploadMaximumSizeException;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.presignedurl.BinaryContentPresignedUrlService;
import com.codeit.otboo.domain.binarycontent.repository.BinaryContentRepository;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentServiceImpl;
import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.properties.MultipartProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.unit.DataSize;

import java.time.LocalDateTime;
import java.util.List;
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

    @Mock
    private BinaryContentPresignedUrlService binaryContentPresignedUrlService;

    @InjectMocks
    private BinaryContentServiceImpl binaryContentService;

    @BeforeEach
    void setUp() {
        MultipartProperties props = new MultipartProperties(DataSize.ofMegabytes(10));

        binaryContentService = new BinaryContentServiceImpl(
                eventPublisher,
                binaryContentRepository,
                binaryContentPresignedUrlService,
                props
        );
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
    
    @Nested
    @DisplayName("Presigned url 발급")
    class DeleteAllBinaryContents {
        @ParameterizedTest
        @CsvSource({
                "test.jpg, image/jpeg",
                "test.png, application/octet-stream",
                "test.jpg, application/octet-stream",
                "test.jpeg, application/octet-stream",
                "test.webp, application/octet-stream",
                "test.gif, application/octet-stream"
        })
        @DisplayName("성공: 유효한 파라미터가 들어올 경우 Presigned url 발급된다.")
        void success_delete_all_binary_contents(String fileName, String contentType) {
            // given
            BinaryContentPresignedUrlRequest request = new BinaryContentPresignedUrlRequest(
                    fileName, contentType, 10L);
            BinaryContent binaryContent = BinaryContentFixture.create();
            String presignedUrl = "https://presigned-url.com";

            given(binaryContentRepository.save(any(BinaryContent.class)))
                    .willReturn(binaryContent);
            given(binaryContentPresignedUrlService.createPresignedUploadUrl(
                    binaryContent.getId(), request.contentType()))
                    .willReturn(presignedUrl);

            // when
            BinaryContentPresignedUrlResponse response = binaryContentService.getPresignedUrl(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.binaryContentId()).isEqualTo(binaryContent.getId());
            assertThat(response.uploadUrl()).isEqualTo(presignedUrl);

            then(binaryContentRepository).should(times(1))
                    .save(any(BinaryContent.class));
            then(binaryContentPresignedUrlService).should(times(1))
                    .createPresignedUploadUrl(binaryContent.getId(), request.contentType());
        }

        @Test
        @DisplayName("실패: 요청 온 파일 사이즈가 maxSize 를 넘을 때 예외가 발생한다")
        void fail_delete_all_binary_contents() {
            // given
            BinaryContentPresignedUrlRequest request = new BinaryContentPresignedUrlRequest(
                    "파일 이름", "image/jpeg", 100000000L);

            // when & then
            assertThatThrownBy(() -> binaryContentService.getPresignedUrl(request))
                    .isInstanceOf(FileUploadMaximumSizeException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FILE_UPLOAD_MAXIMUM_SIZE);
        }

        @Test
        @DisplayName("실패: 타입이 비정상적일 경우 예외가 발생한다")
        void fail_delete_all_binary_contents_invalid_content_type() {
            // given
            BinaryContentPresignedUrlRequest request = new BinaryContentPresignedUrlRequest(
                    "파일 이름", "noType", 10L);

            // when & then
            assertThatThrownBy(() -> binaryContentService.getPresignedUrl(request))
                    .isInstanceOf(FileTypeNotSupportException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_FILE_TYPE);
        }

        @Test
        @DisplayName("실패: 타입이 application/octet-stream 이고, 파일명의 확장자가 명확하지 않은 경우 예외가 발생한다")
        void fail_delete_all_binary_content_invalid_content_type_octet_stream() {
            // given
            BinaryContentPresignedUrlRequest request = new BinaryContentPresignedUrlRequest(
                    "파일 이름", "application/octet-stream", 10L);

            // when & then
            assertThatThrownBy(() -> binaryContentService.getPresignedUrl(request))
                    .isInstanceOf(FileTypeNotSupportException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    @Nested
    @DisplayName("기준 시간 이전이며, PROCESSING 상태인 BinaryContent를 모두 삭제")
    class DeleteAllBinaryContentsByTime {
        @Test
        @DisplayName("성공: 기준 시간 이전의 PROCESSING인 BinaryContent가 없을 경우 바로 메소드를 종료한다")
        void success_delete_all_binary_contents_not_exist(){
            // given
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(1);

            given(binaryContentRepository.findAllByUploadStatusAndCreatedAtBefore(
                    eq(UploadStatus.PROCESSING), any(LocalDateTime.class)
            )).willReturn(List.of());

            // when
            binaryContentService.deleteAllStaleProcessingBinaryContents(cutoffTime);

            // then
            then(binaryContentRepository).should(never())
                    .deleteAllById(any());
            then(eventPublisher).should(never())
                    .publishEvent(any(BinaryContentDeletedEvent.class));
        }

        @Test
        @DisplayName("성공: 기준 시간 이전의 PROCESSING인 BinaryContent들이 있으면, binaryContent")
        void success_delete_all_binary_content(){
            // given
            LocalDateTime cutoffTime = LocalDateTime.now();
            List<BinaryContent> targetList = List.of(
                    BinaryContentFixture.create(LocalDateTime.now().minusMinutes(1)),
                    BinaryContentFixture.create(LocalDateTime.now().minusMinutes(2)),
                    BinaryContentFixture.create(LocalDateTime.now().minusMinutes(2))
            );
            List<UUID> targetIdList = targetList.stream().map(BinaryContent::getId).toList();

            given(binaryContentRepository.findAllByUploadStatusAndCreatedAtBefore(
                    UploadStatus.PROCESSING, cutoffTime
            )).willReturn(targetList);

            // when
            binaryContentService.deleteAllStaleProcessingBinaryContents(cutoffTime);

            // then
            then(binaryContentRepository).should(times(1))
                    .deleteAllById(targetIdList);
            then(eventPublisher).should(times(1))
                    .publishEvent(any(BinaryContentListDeletedEvent.class));
        }
    }

    @Nested
    @DisplayName("업로드 성공 상태로 업데이트")
    class UpdateUploadStatus {
        @Test
        @DisplayName("성공: 존재하는 BinaryContent의 ID 가 들어올 경우 정상적으로 SUCCESS 로 업데이트된다")
        void success_completeUpload(){
            // given
            BinaryContent binaryContent = BinaryContentFixture.create();
            given(binaryContentRepository.findById(any(UUID.class)))
                    .willReturn(Optional.of(binaryContent));

            // when
            binaryContentService.completeUpload(binaryContent.getId());

            // then
            assertThat(binaryContent.getUploadStatus()).isEqualTo(UploadStatus.SUCCESS);
            then(binaryContentRepository).should(times(1))
                    .findById(binaryContent.getId());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 BinaryContent의 ID 가 들어올 경우 예외가 발생한다")
        void fail_binaryContent_NotFound(){
            // given
            given(binaryContentRepository.findById(any(UUID.class)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> binaryContentService.completeUpload(UUID.randomUUID()))
                    .isInstanceOf(BinaryContentNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BINARY_CONTENT_NOT_FOUND);
        }
    }
}
