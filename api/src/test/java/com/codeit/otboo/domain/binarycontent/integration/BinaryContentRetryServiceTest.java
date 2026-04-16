package com.codeit.otboo.domain.binarycontent.integration;

import com.codeit.otboo.domain.binarycontent.service.BinaryContentRetryService;
import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import com.codeit.otboo.global.config.TestRetryConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest(
        classes = {
                BinaryContentRetryService.class,
                TestRetryConfig.class
        }
)
public class BinaryContentRetryServiceTest {
    @Autowired
    private BinaryContentRetryService binaryContentRetryService;

    @MockitoBean
    private BinaryContentStorage binaryContentStorage;

    @Nested
    @DisplayName("단건 삭제")
    class Delete {
        @Test
        @DisplayName("성공: 예외가 없으면 1번만 호출된다")
        void success_delete_once() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            // when
            binaryContentRetryService.delete(binaryContentId);

            // then
            then(binaryContentStorage).should(times(1)).delete(binaryContentId);
        }

        @Test
        @DisplayName("성공: 계속 실패하면 3번 재시도 후 recover 로 종료된다")
        void success_delete_retry_and_recover() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            willThrow(new RuntimeException("삭제 강제 실패 처리"))
                    .given(binaryContentStorage)
                    .delete(binaryContentId);

            // when & then
            // Recover로 인하여 delete 할 때 내부에서 예외가 계속 발생해도 예외가 밖으로 던지지 않는 것을 검증
            assertThatCode(() -> binaryContentRetryService.delete(binaryContentId))
                    .doesNotThrowAnyException();

            then(binaryContentStorage).should(times(3)).delete(binaryContentId);
        }

        @Test
        @DisplayName("성공: 2번 실패 후 3번째에 성공하면 총 3번 호출된다")
        void success_delete_retry_until_success() {
            // given
            UUID binaryContentId = UUID.randomUUID();

            willAnswer(invocation -> {
                throw new RuntimeException("첫 번째 실패");
            }).willAnswer(invocation -> {
                throw new RuntimeException("두 번째 실패");
            }).willAnswer(invocation -> null)
                .given(binaryContentStorage)
                .delete(binaryContentId);

            // when & then
            assertThatCode(() -> binaryContentRetryService.delete(binaryContentId))
                    .doesNotThrowAnyException();

            then(binaryContentStorage).should(times(3)).delete(binaryContentId);
        }
    }

    @Nested
    @DisplayName("다건 삭제 재시도")
    class DeleteAllRetry {

        @Test
        @DisplayName("성공: 빈 리스트면 storage 를 호출하지 않고 종료한다")
        void success_delete_all_empty() {
            // given
            List<UUID> binaryContentIds = List.of();

            // when
            binaryContentRetryService.deleteAll(binaryContentIds);

            // then
            then(binaryContentStorage).should(never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("성공: 계속 실패하면 3번 재시도 후 recover 로 종료된다")
        void success_delete_all_retry_and_recover() {
            // given
            List<UUID> binaryContentIds = List.of(UUID.randomUUID());

            willThrow(new RuntimeException("모두 삭제 실패"))
                    .given(binaryContentStorage)
                    .deleteAll(binaryContentIds);

            // when & then
            assertThatCode(() -> binaryContentRetryService.deleteAll(binaryContentIds))
                    .doesNotThrowAnyException();

            then(binaryContentStorage).should(times(3)).deleteAll(binaryContentIds);
        }

        @Test
        @DisplayName("성공: 1000건 초과면 1000개 단위로 나누어 삭제한다")
        void success_delete_all_chunk() {
            // given
            int chunkSize = 1000;
            int totalsize = 2002;
            int expectedChunkCount = (int) Math.ceil((double) totalsize / chunkSize);
            List<UUID> binaryContentIds = new ArrayList<>();
            for (int i = 0; i < totalsize; i++) {
                binaryContentIds.add(UUID.randomUUID());
            }

            // when
            binaryContentRetryService.deleteAll(binaryContentIds);

            // then
            ArgumentCaptor<List<UUID>> captor = ArgumentCaptor.forClass(List.class);
            then(binaryContentStorage).should(times(expectedChunkCount)).deleteAll(captor.capture());

            List<List<UUID>> allValues = captor.getAllValues();
            assertThat(allValues).hasSize(expectedChunkCount);
            assertThat(allValues.get(0)).hasSize(chunkSize);
            assertThat(allValues.get(1)).hasSize(chunkSize);
            assertThat(allValues.get(2)).hasSize(totalsize % chunkSize);
        }
    }
}
