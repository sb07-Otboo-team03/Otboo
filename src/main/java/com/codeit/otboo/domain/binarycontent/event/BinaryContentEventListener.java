package com.codeit.otboo.domain.binarycontent.event;

import com.codeit.otboo.domain.binarycontent.service.BinaryContentRetryService;
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
    private final BinaryContentRetryService binaryContentRetryService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreated(BinaryContentCreatedEvent event){
        binaryContentRetryService.upload(event.binaryContentId(), event.bytes(), event.contentType());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleted(BinaryContentDeletedEvent event){
        binaryContentRetryService.delete(event.binaryContentId());
    }
}