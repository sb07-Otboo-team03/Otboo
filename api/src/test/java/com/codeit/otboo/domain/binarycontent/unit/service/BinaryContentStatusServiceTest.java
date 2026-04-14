package com.codeit.otboo.domain.binarycontent.unit.service;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.entity.UploadStatus;
import com.codeit.otboo.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.repository.BinaryContentRepository;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentStatusService;
import com.codeit.otboo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class BinaryContentStatusServiceTest {
    @Mock
    private BinaryContentRepository binaryContentRepository;

    @InjectMocks
    private BinaryContentStatusService binaryContentStatusService;

    @Nested
    @DisplayName("성공 상태 업데이트")
    class updateSuccess{
        @Test
        @DisplayName("성공: 유효한 이미지 ID가 들어올 경우 SUCCESS 상태로 업데이트 된다")
        void updateSuccess_Success(){
            // given
            BinaryContent binaryContent = BinaryContentFixture.create();
            given(binaryContentRepository.findById(binaryContent.getId()))
                    .willReturn(Optional.of(binaryContent));

            // when
            binaryContentStatusService.updateSuccess(binaryContent.getId());

            // then
            assertThat(binaryContent.getUploadStatus()).isEqualTo(UploadStatus.SUCCESS);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 바이너리 ID 가 들어올 경우 예외가 발생한다")
        void updateSuccess_Fail(){
            // given
            UUID binaryContentId = UUID.randomUUID();
            given(binaryContentRepository.findById(binaryContentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> binaryContentStatusService.updateSuccess(binaryContentId))
                    .isInstanceOf(BinaryContentNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BINARY_CONTENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("실패 상태 업데이트")
    class updateFail{
        @Test
        @DisplayName("성공: 유효한 이미지 ID가 들어올 경우 FAIL 상태로 업데이트 된다")
        void updateSuccess_Success(){
            // given
            BinaryContent binaryContent = BinaryContentFixture.create();
            given(binaryContentRepository.findById(binaryContent.getId()))
                    .willReturn(Optional.of(binaryContent));

            // when
            binaryContentStatusService.updateFail(binaryContent.getId());

            // then
            assertThat(binaryContent.getUploadStatus()).isEqualTo(UploadStatus.FAIL);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 바이너리 ID 가 들어올 경우 예외가 발생한다")
        void updateSuccess_Fail(){
            // given
            UUID binaryContentId = UUID.randomUUID();
            given(binaryContentRepository.findById(binaryContentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> binaryContentStatusService.updateFail(binaryContentId))
                    .isInstanceOf(BinaryContentNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BINARY_CONTENT_NOT_FOUND);
        }
    }
}
