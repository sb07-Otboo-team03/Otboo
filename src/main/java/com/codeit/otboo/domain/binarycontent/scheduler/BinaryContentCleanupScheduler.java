package com.codeit.otboo.domain.binarycontent.scheduler;

import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentCleanupScheduler {
    private final BinaryContentService binaryContentService;

    // 매일 새벽 4시마다 1시간 전의 PROCESS 상태 데이터 삭제
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void cleanupStaleProcessingBinaryContents() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);

        log.debug("Start cleanup stale processing binary contents. cutoff={}", cutoff);

        binaryContentService.deleteAllStaleProcessingBinaryContents(cutoff);

        log.debug("Finish cleanup stale processing binary contents.");
    }
}
