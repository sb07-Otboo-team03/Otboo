package com.codeit.otboo.domain.user.listener;

import com.codeit.otboo.domain.user.event.TemporaryPasswordIssuedEvent;
import com.codeit.otboo.domain.user.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class TemporaryPasswordIssuedEventListener {
    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TemporaryPasswordIssuedEvent event) {
        log.debug("메일 발송");
        String email = event.email();
        String temporaryPassword = event.temporaryPassword();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expiresAt = event.expiresAt().format(formatter);
        mailService.sendTemporaryPassword(email, temporaryPassword, expiresAt);
    }
}
