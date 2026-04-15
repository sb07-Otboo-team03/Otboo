package com.codeit.otboo.domain.kafka;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.sse.event.FeedCreatedEvent;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.domain.websocket.event.DirectMessageCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.core.KafkaTemplate;

@TestConfiguration
@ExtendWith(MockitoExtension.class)
@DisplayName("🎯Unit Test> KafkaProduceRequiredEventListener")
class KafkaProduceRequiredEventListenerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private UserService userService;

    @Spy
    private ObjectMapper objectMapper;


    private KafkaProduceRequiredEventListener listener;

    private DirectMessageResponse response;
    private DirectMessageCreatedEvent event;

    @BeforeEach
    void setUp() {

        objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

        listener = new KafkaProduceRequiredEventListener(
                kafkaTemplate,
                objectMapper,
                notificationService,
                notificationMapper,
                userService
        );

        response = DirectMessageResponse.builder()
            .id(UUID.randomUUID())
            .createdAt(LocalDateTime.now())
            .sender(new UserSummaryResponse(
                UUID.randomUUID(),
                "sender",
                "sender.png"
            ))
            .receiver(new UserSummaryResponse(
                UUID.randomUUID(),
                "receiver",
                "receiver.png"
            ))
            .content("hello")
            .build();

        event = new DirectMessageCreatedEvent(response, LocalDateTime.now());
    }

//    @Test
//    void DirectMessageCreatedEvent_전송_성공() throws Exception {
//        // given
//        // when
//        listener.on(event);
//
//        // then
//        verify(kafkaTemplate)
//            .send(eq("otboo.DirectMessageCreatedEvent"), anyString());
//    }
//
//    @Test
//    void FeedCreatedEvent_전송_성공() {
//        // given
//        FeedCreatedEvent event = new FeedCreatedEvent(
//            "피드 제목",
//            "피드 내용",
//            List.of(UUID.randomUUID(), UUID.randomUUID())
//        );
//
//        // when
//        listener.on(event);
//
//        // then
//        verify(kafkaTemplate)
//            .send(eq("otboo.FeedCreatedEvent"), anyString());
//    }

    @Test
    void JSON_직렬화_실패시_RuntimeException() throws Exception {
        // given
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        KafkaProduceRequiredEventListener listener =
            new KafkaProduceRequiredEventListener(
                    kafkaTemplate,
                    mockMapper,
                    notificationService,
                    notificationMapper,
                    userService
            );

        when(mockMapper.writeValueAsString(any()))
            .thenThrow(new JsonProcessingException("error") {});

        // when & then
        assertThrows(RuntimeException.class, () -> listener.on(event));
    }
}