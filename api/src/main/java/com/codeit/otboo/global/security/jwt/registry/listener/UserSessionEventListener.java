package com.codeit.otboo.global.security.jwt.registry.listener;

import com.codeit.otboo.global.security.jwt.registry.RedisRegistry;
import com.codeit.otboo.global.security.jwt.registry.event.SessionDeletedRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserSessionEventListener {
    private final RedisRegistry redisRegistry;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SessionDeletedRequestEvent event) {
        redisRegistry.delete(event.userId());
        log.info("사용자 세션 무효화: {}", event.reason());
    }

}
