package com.codeit.otboo.domain.sse.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

    private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

    public SseEmitter save(UUID receiverId, SseEmitter sseEmitter) {
        data.compute(receiverId, (key, emitters) -> {
            if (emitters == null) {
                return new CopyOnWriteArrayList<>(List.of(sseEmitter));
            } else {
                emitters.add(sseEmitter);
                return emitters;
            }
        });

        return sseEmitter;
    }

    public Optional<List<SseEmitter>> findByReceiverId(UUID receiverId) {
        return Optional.ofNullable(data.get(receiverId));
    }

    public List<SseEmitter> findAllByReceiverIdsIn(Collection<UUID> receiverIds) {
        return data.entrySet().stream()
            .filter(entry -> receiverIds.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .flatMap(Collection::stream)
            .toList();
    }

    public List<SseEmitter> findAll() {
        return data.values().stream()
            .flatMap(Collection::stream)
            .toList();
    }

    public void delete(UUID receiverId, SseEmitter sseEmitter) {
        data.computeIfPresent(receiverId, (key, emitters) -> {
            emitters.remove(sseEmitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
