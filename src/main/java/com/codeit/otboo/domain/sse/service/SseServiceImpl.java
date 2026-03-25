package com.codeit.otboo.domain.sse.service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.sse.object.SseMessage;
import com.codeit.otboo.domain.sse.repository.SseEmitterRepository;
import com.codeit.otboo.domain.sse.repository.SseMessageRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    @Value("${sse.timeout}")
    private long timeout;

    private final SseEmitterRepository sseEmitterRepository;
    private final SseMessageRepository sseMessageRepository;

    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        SseEmitter emitter = new SseEmitter(timeout);

        emitter.onCompletion(() -> {
            log.debug("✅sse on onCompletion");
            sseEmitterRepository.delete(receiverId, emitter);
        });
        emitter.onTimeout(() -> {
            log.debug("✅sse on onTimeout");
            sseEmitterRepository.delete(receiverId, emitter);
        });
        emitter.onError((ex) -> {
            log.debug("✅sse on onError");
            sseEmitterRepository.delete(receiverId, emitter);
        });

        boolean success;

        if (lastEventId != null) {
            success = replay(emitter, receiverId, lastEventId);
        } else {
            success = ping(emitter);
            if (!success) {
                emitter.completeWithError(new RuntimeException("initial ping failed"));
            }
        }

        if (success) {
            sseEmitterRepository.save(receiverId, emitter);
        }

        return emitter;
    }

    private boolean replay(SseEmitter emitter, UUID receiverId, UUID lastEventId) {
        List<SseMessage> messages =
            sseMessageRepository.findAllByEventIdAfterAndReceiverId(lastEventId, receiverId);

        for (SseMessage msg : messages) {
            try {
                emitter.send(msg.toEvent());
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                emitter.completeWithError(e);
                return false;
            }
        }
        return true;
    }

    public void send(Collection<UUID> receiverIds, String eventName, NotificationDto data) {
        SseMessage message = sseMessageRepository.save(SseMessage.create(receiverIds, eventName, data));
        Set<DataWithMediaType> event = message.toEvent();
        List<SseEmitter> emitters =
            new ArrayList<>(sseEmitterRepository.findAllByReceiverIdsIn(receiverIds));

        emitters.forEach(emitter -> {
            try {
                emitter.send(event);
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                emitter.completeWithError(e);
            }
        });
    }

    public void broadcast(String eventName, NotificationDto data) {
        SseMessage message = sseMessageRepository.save(SseMessage.createBroadcast(eventName, data));
        Set<DataWithMediaType> event = message.toEvent();
        List<SseEmitter> emitters =
            new ArrayList<>(sseEmitterRepository.findAll());

        emitters.forEach(emitter -> {
            try {
                emitter.send(event);
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                emitter.completeWithError(e);
            }
        });
    }

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanUp() {
        List<SseEmitter> emitters =
            new ArrayList<>(sseEmitterRepository.findAll());

        emitters.stream()
            .filter(emitter -> !ping(emitter))
            .forEach(emitter ->
                emitter.completeWithError(new RuntimeException("sse ping failed")));
    }

    private boolean ping(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event()
                .name("ping")
                .build());
            return true;
        } catch (IOException e) {
            log.warn("Failed to send ping event", e);
//            sseEmitter.completeWithError(e);
            return false;
        }
    }
}
