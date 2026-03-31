package com.codeit.otboo.domain.binarycontent.event;

import com.codeit.otboo.domain.binarycontent.service.BinaryContentStatusService;
import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {
    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentStatusService binaryContentStatusService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BinaryContentCreatedEvent event) {
        try {
            binaryContentStorage.put(event.binaryContentId(), event.bytes());
            binaryContentStatusService.updateSuccess(event.binaryContentId());
        } catch (Exception e) {
            log.error("해당 파일 업로드에 실패하였습니다. id={}", event.binaryContentId(), e);
            binaryContentStatusService.updateFail(event.binaryContentId());
        }
    }
}