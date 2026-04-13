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
    private final BinaryContentStorage binaryContentStorage;

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000) // 2초 간격
    )
    public void delete(UUID binaryContentId) {
        binaryContentStorage.delete(binaryContentId);
    }

    @Recover
    public void recoverDelete(Exception e, UUID binaryContentId) {
        log.error(
                "Delete Fail | BinaryContentId={} | Error={}",
                binaryContentId,
                e.getMessage()
        );
    }
}