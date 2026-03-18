package com.codeit.otboo.domain.sse.service;

import java.util.Collection;
import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter connect(UUID lastEventId);
    void send(Collection<UUID> receiverIds, String eventName, Object data);
    void broadcast(String eventName, Object data);
    void cleanUp();
}
