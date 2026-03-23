package com.codeit.otboo.domain.sse.service;

import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseServiceImpl implements SseService {
    @Value("${sse.timeout}")
    private Long timeout;

    @Override
    public SseEmitter connect(UUID lastEventId) {
        SseEmitter emitter = new SseEmitter(timeout);

        return emitter;
    }

    @Override
    public void send(Collection<UUID> receiverIds, String eventName, Object data) {

    }

    @Override
    public void broadcast(String eventName, Object data) {

    }

    @Override
    public void cleanUp() {

    }
}
