package com.codeit.otboo.global;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuntimePropertyLogger implements ApplicationRunner {

    private final Environment env;

    @Override
    public void run(ApplicationArguments args) {
        log.info("runtime property: server.forward-headers-strategy={}",
                env.getProperty("server.forward-headers-strategy"));
        log.info("runtime property: spring.profiles.active={}",
                env.getProperty("spring.profiles.active"));
    }
}