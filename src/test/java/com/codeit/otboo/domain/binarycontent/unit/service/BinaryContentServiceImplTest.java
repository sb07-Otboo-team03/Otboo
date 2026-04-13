package com.codeit.otboo.domain.binarycontent.unit.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentPresignedUrlRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentDeletedEvent;
import com.codeit.otboo.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.codeit.otboo.domain.binarycontent.exception.FileUploadMaximumSizeException;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.repository.BinaryContentRepository;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentPresignedUrlService;
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
}
