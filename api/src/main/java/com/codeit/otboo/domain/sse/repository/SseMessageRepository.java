package com.codeit.otboo.domain.sse.repository;

import com.codeit.otboo.domain.sse.object.SseMessage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

    @Value("${sse.event-queue-capacity:100}")
    private int eventQueueCapacity;

    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public SseMessage save(SseMessage message) {
        makeAvailableCapacity();

        UUID eventId = message.getEventId();
        eventIdQueue.addLast(eventId);
        messages.put(eventId, message);
        return message;
    }

    public List<SseMessage> findAllByEventIdAfterAndReceiverId(UUID eventId, UUID receiverId) {
        return eventIdQueue.stream()
            .dropWhile(data -> !data.equals(eventId))
            .skip(1)
            .map(messages::get)
            .filter(sseMessage -> sseMessage.isReceivable(receiverId))
            .toList();
    }

    private void makeAvailableCapacity() {
        int availableCapacity = eventQueueCapacity - eventIdQueue.size();
        while (availableCapacity < 1) {
            UUID removedEventId = eventIdQueue.removeFirst();
            messages.remove(removedEventId);
            availableCapacity++;
        }
    }
}
