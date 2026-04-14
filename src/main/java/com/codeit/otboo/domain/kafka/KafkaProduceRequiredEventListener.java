package com.codeit.otboo.domain.kafka;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.sse.event.BaseSseEvent;
import com.codeit.otboo.domain.sse.event.ClothesAttributeDefSseEvent;
import com.codeit.otboo.domain.sse.event.CommentCreatedEvent;
import com.codeit.otboo.domain.sse.event.DirectMessageSseEvent;
import com.codeit.otboo.domain.sse.event.FeedCreatedEvent;
import com.codeit.otboo.domain.sse.event.FeedLikedEvent;
import com.codeit.otboo.domain.sse.event.FollowSseEvent;
import com.codeit.otboo.domain.sse.event.UserRoleUpdatedEvent;
import com.codeit.otboo.domain.sse.event.WeatherSseEvent;
import com.codeit.otboo.domain.websocket.event.DirectMessageCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
//@Profile("prod")
@Component
public class KafkaProduceRequiredEventListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async
    @TransactionalEventListener
    public void on(DirectMessageCreatedEvent event) {
        sendToKafkaWithWebSocketKey(event);
    }

    @Async
    @TransactionalEventListener
    public void on(DirectMessageSseEvent event) {
        sendToKafkaWithSseKey(event);
    }

    @Async
    @TransactionalEventListener
    public void on(FollowSseEvent event) { sendToKafkaWithSseKey(event);}

    @Async
    @TransactionalEventListener
    public void on(CommentCreatedEvent event) {
        sendToKafkaWithSseKey(event);
    }

    @Async
    @TransactionalEventListener
    public void on(FeedLikedEvent event) {
        sendToKafkaWithSseKey(event);
    }

    @Async
    @TransactionalEventListener
    public void on(UserRoleUpdatedEvent event) {
        sendToKafkaWithSseKey(event);
    }

    @Async
    @TransactionalEventListener
    public void on(FeedCreatedEvent event) {
        sendToKafkaWithSseKey(event);
    }

    @Async
    @TransactionalEventListener
    public void on(ClothesAttributeDefSseEvent event) {
        sendToKafkaWithSseKey(event);
    }

    @Async
    @TransactionalEventListener
    public void on(WeatherSseEvent event) {
        sendToKafkaWithSseKey(event);
    }

    private <T> void sendToKafkaWithSseKey(T event) {
        try {
            String topic = "otboo." + event.getClass().getSimpleName();
            String message = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, topic, message);
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send event to Kafka", e);
            throw new RuntimeException(e);
        }
    }

    private <T> void sendToKafkaWithWebSocketKey(DirectMessageCreatedEvent event) {
        try {
            String topic = "otboo." + event.getClass().getSimpleName();
            String message = objectMapper.writeValueAsString(event);

            DirectMessageResponse directMessageResponse = event.getData();
            String webSocketKey = KafkaUtil.makeWebSocketKey(directMessageResponse);

            kafkaTemplate.send(topic, webSocketKey, message);
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send event to Kafka", e);
            throw new RuntimeException(e);
        }
    }
}
