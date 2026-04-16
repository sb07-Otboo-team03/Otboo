package com.codeit.otboo.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final AdminBootstrapService adminBootstrapService;

    @EventListener(ApplicationReadyEvent.class)
    public void adminInit() {
        log.info("Admin account initialization");
        adminBootstrapService.initialize();
    }
}
