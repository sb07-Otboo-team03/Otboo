package com.codeit.otboo.domain.kafka;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
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
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
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

//    @Async
//    @TransactionalEventListener
//    public void on(BinaryContentCreatedEvent event) { sendToKafka(event);}
//
//    @Async
//    @TransactionalEventListener
//    public void on(BinaryContentDeletedEvent event) {
//        sendToKafka(event);
//    }

    @Async
    @TransactionalEventListener
    public void on(DirectMessageCreatedEvent event) {
        sendToKafkaWithKey(event);
    }

    @Async
    @TransactionalEventListener
    public void on(DirectMessageSseEvent event) {
        sendToKafka(event);
    }

    @Async
    @TransactionalEventListener
    public void on(FollowSseEvent event) {
        sendToKafka(event);
    }

    @Async
    @TransactionalEventListener
    public void on(CommentCreatedEvent event) {
        sendToKafka(event);
    }

    @Async
    @TransactionalEventListener
    public void on(FeedLikedEvent event) {
        sendToKafka(event);
    }

    @Async
    @TransactionalEventListener
    public void on(FeedCreatedEvent event) {
        sendToKafka(event);
    }

    @Async
    @TransactionalEventListener
    public void on(ClothesAttributeDefSseEvent event) {
        sendToKafka(event);
    }

    @Async
    @TransactionalEventListener
    public void on(WeatherSseEvent event) {
        sendToKafka(event);
    }

    @Async
    @TransactionalEventListener
    public void on(UserRoleUpdatedEvent event) {
        sendToKafka(event);
    }


    private <T> void sendToKafka(T event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            String topic = "otboo." + event.getClass().getSimpleName();
            kafkaTemplate.send(topic, message);
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send event to Kafka", e);
            throw new RuntimeException(e);
        }
    }

    private <T> void sendToKafkaWithKey(DirectMessageCreatedEvent event) {
        try {
            DirectMessageResponse directMessageResponse = event.getData();
            String senderId = directMessageResponse.sender().userId().toString();
            String receiverId = directMessageResponse.receiver().userId().toString();

            String key = senderId + "+" + receiverId;

            String message = objectMapper.writeValueAsString(event);
            String topic = "otboo." + event.getClass().getSimpleName();
            kafkaTemplate.send(topic, key, message);
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send event to Kafka", e);
            throw new RuntimeException(e);
        }
    }
}
