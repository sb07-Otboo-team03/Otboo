package com.codeit.otboo.domain.binarycontent.unit.scheduler;

import com.codeit.otboo.domain.binarycontent.scheduler.BinaryContentCleanupScheduler;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

@ExtendWith(MockitoExtension.class)
public class CleanupStaleProcessingBinaryContentsTest {
    @Mock
    private BinaryContentService binaryContentService;

    @InjectMocks
    private BinaryContentCleanupScheduler binaryContentCleanupScheduler;

    @Nested
    @DisplayName("PROCESS상태의 BinaryContent 정리")
    class CleanupStaleProcessingBinaryContents {
        @Test
        @DisplayName("성공: cleanup 메서드에 스케줄 설정이 되어 있다")
        void success_scheduled_annotation_exists() throws NoSuchMethodException {
            // given
            Method method = BinaryContentCleanupScheduler.class
                    .getMethod("cleanupStaleProcessingBinaryContents");

            // when
            Scheduled scheduled = method.getAnnotation(Scheduled.class);

            // then
            assertThat(scheduled).isNotNull();
            assertThat(scheduled.cron()).isEqualTo("0 0 4 * * *");
            assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
        }

        @Test
        @DisplayName("성공: 현재 시각 기준 1시간 전 cutoff 로 삭제를 요청한다")
        void success_cleanup_stale_processing_binary_contents() {
            // given
            LocalDateTime beforeCall = LocalDateTime.now();

            // when
            binaryContentCleanupScheduler.cleanupStaleProcessingBinaryContents();

            // then
            ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);

            then(binaryContentService).should(times(1))
                    .deleteAllStaleProcessingBinaryContents(captor.capture());

            LocalDateTime actual = captor.getValue();
            LocalDateTime afterCall = LocalDateTime.now();

            // now() 호출 시점과 메소드 호출시점 차이로 경계 시간(예: 03:59:59 → 04:00:00)에서 테스트가 깨질 수 있어 범위로 검증
            assertThat(actual).isBetween(
                    beforeCall.minusHours(1),
                    afterCall.minusHours(1)
            );
        }
    }
}
