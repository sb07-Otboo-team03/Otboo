package com.codeit.otboo.domain.binarycontent.service;

import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void deleteAll(List<UUID> binaryContentIds) {
        log.info("Bulk Delete Count={}", binaryContentIds.size());

        final int chunkSize = 1000;
        if (binaryContentIds.isEmpty()) {
            return;
        }

        for (int i = 0; i < binaryContentIds.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, binaryContentIds.size());
            List<UUID> chunk = binaryContentIds.subList(i, end);
            binaryContentStorage.deleteAll(chunk);
        }
    }

    @Recover
    public void recoverDeleteAll(Exception e, List<UUID> binaryContentIds) {
        log.error(
                "Bulk Delete Fail | Count={} | Error={}",
                binaryContentIds.size(),
                e.getMessage()
        );
    }
}