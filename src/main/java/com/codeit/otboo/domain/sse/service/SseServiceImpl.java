package com.codeit.otboo.domain.sse.service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.sse.object.SseMessage;
import com.codeit.otboo.domain.sse.repository.SseEmitterRepository;
import com.codeit.otboo.domain.sse.repository.SseMessageRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
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
        SseEmitter sseEmitter = new SseEmitter(timeout);

        sseEmitter.onCompletion(() -> {
            log.debug("✅sse on onCompletion");
            sseEmitterRepository.delete(receiverId, sseEmitter);
        });
        sseEmitter.onTimeout(() -> {
            log.debug("✅sse on onTimeout");
            sseEmitterRepository.delete(receiverId, sseEmitter);
        });
        sseEmitter.onError((ex) -> {
            log.debug("✅sse on onError");
            sseEmitterRepository.delete(receiverId, sseEmitter);
        });

        sseEmitterRepository.save(receiverId, sseEmitter);

        Optional.ofNullable(lastEventId)
            .ifPresentOrElse(
                id -> {
                    sseMessageRepository.findAllByEventIdAfterAndReceiverId(id, receiverId)
                        .forEach(sseMessage -> {
                            try {
                                sseEmitter.send(sseMessage.toEvent());
                            } catch (IOException e) {
                                log.warn( e.getMessage(), e);
                            }
                        });
                },
                () -> {
                    ping(sseEmitter);
                }
            );

        return sseEmitter;
    }

    public void send(Collection<UUID> receiverIds, String eventName, NotificationDto data) {
        SseMessage message = sseMessageRepository.save(SseMessage.create(receiverIds, eventName, data));
        Set<DataWithMediaType> event = message.toEvent();
        sseEmitterRepository.findAllByReceiverIdsIn(receiverIds)
            .forEach(sseEmitter -> {
                try {
                    sseEmitter.send(event);
                } catch (IOException e) {
                    log.warn( e.getMessage(), e);
                }
            });
    }

    public void broadcast(String eventName, NotificationDto data) {
        SseMessage message = sseMessageRepository.save(SseMessage.createBroadcast(eventName, data));
        Set<DataWithMediaType> event = message.toEvent();
        sseEmitterRepository.findAll()
            .forEach(sseEmitter -> {
                try {
                    sseEmitter.send(event);
                } catch (IOException e) {
                    log.warn( e.getMessage(), e);
                }
            });
    }

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanUp() {
        sseEmitterRepository.findAll()
            .stream().filter(sseEmitter -> !ping(sseEmitter))
            .forEach(
                sseEmitter -> sseEmitter.completeWithError(new RuntimeException("sse ping failed")));
    }

    private boolean ping(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event()
                .name("ping")
                .build());
            return true;
        } catch (IOException e) {
            log.warn("Failed to send ping event", e);
            return false;
        }
    }
}
