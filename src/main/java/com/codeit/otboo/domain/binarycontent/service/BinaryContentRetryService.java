package com.codeit.otboo.domain.binarycontent.service;

import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinaryContentRetryService {
    private final BinaryContentStorage storage;
    private final BinaryContentStatusService binaryContentStatusService;

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000) // 2초 간격
    )
    public void upload(UUID binaryContentId, byte[] bytes) {
        log.info("파일 업로드 시도 id={}", binaryContentId);
        storage.put(binaryContentId, bytes);
        binaryContentStatusService.updateSuccess(binaryContentId);
    }

    @Recover
    public void recover(Exception e, UUID binaryContentId) {
        log.error("파일 업로드 최종 실패 id={}", binaryContentId, e);
        log.error(
                "Upload Fail | BinaryContentId={} | Error={}",
                binaryContentId,
                e.getMessage()
        );
        binaryContentStatusService.updateFail(binaryContentId);
    }
}
