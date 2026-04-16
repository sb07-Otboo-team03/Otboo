package com.codeit.otboo.domain.sse.service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import java.util.Collection;
import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter connect(UUID receiverId, UUID lastEventId);
    void send(Collection<UUID> receiverIds, String eventName, NotificationDto data);
    void broadcast(String eventName, NotificationDto data);
    void cleanUp();
}
