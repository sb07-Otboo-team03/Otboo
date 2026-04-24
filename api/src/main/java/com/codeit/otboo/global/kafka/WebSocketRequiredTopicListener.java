package com.codeit.otboo.global.kafka;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.global.websocket.event.DirectMessageCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketRequiredTopicListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.REALTIME_DIRECT_MESSAGE, groupId = "otboo-websocket-${app.instance-id}")
    public void onDirectMessageCreatedEvent(String kafkaEvent) {
        try {
            DirectMessageCreatedEvent event = objectMapper.readValue(kafkaEvent,
                DirectMessageCreatedEvent.class);

            DirectMessageResponse directMessageResponse = event.getData();
            String directMessageKey = DirectMessageKeyGenerator.generate(directMessageResponse);

            String destination = String.format("/sub/direct-messages_%s", directMessageKey);
            messagingTemplate.convertAndSend(destination, directMessageResponse);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}