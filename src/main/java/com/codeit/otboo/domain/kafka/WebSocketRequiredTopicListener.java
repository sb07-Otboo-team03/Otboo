package com.codeit.otboo.domain.kafka;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.websocket.event.DirectMessageCreatedEvent;
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

    public static String makeWebSocketKey(DirectMessageResponse directMessageResponse) {

        String senderId = directMessageResponse.sender().userId().toString();
        String receiverId = directMessageResponse.receiver().userId().toString();

        return (senderId.compareTo(receiverId) < 0) ?
            senderId + "_" + receiverId :
            receiverId + "_" + senderId;
    }

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "otboo.DirectMessageCreatedEvent", groupId = "websocket-${random.uuid}")
    public void onDirectMessageCreatedEvent(String kafkaEvent) {

        DirectMessageCreatedEvent event = null;
        try {
            event = objectMapper.readValue(kafkaEvent, DirectMessageCreatedEvent.class);

            DirectMessageResponse directMessageResponse = event.getData();
            String websocketKey = WebSocketRequiredTopicListener.makeWebSocketKey(directMessageResponse);
            String destination = String.format("/sub/direct-messages_%s", websocketKey);

            messagingTemplate.convertAndSend(destination, directMessageResponse);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
