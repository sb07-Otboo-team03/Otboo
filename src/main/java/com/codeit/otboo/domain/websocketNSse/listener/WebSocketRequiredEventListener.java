package com.codeit.otboo.domain.websocketNSse.listener;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageCursorResponse;
import com.codeit.otboo.domain.websocketNSse.event.DirectMessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketRequiredEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(DirectMessageCreatedEvent event) {
        DirectMessageCursorResponse directMessageResponse = event.getData();

        String senderId = directMessageResponse.sender().userId().toString();
        String receiverId = directMessageResponse.receiver().userId().toString();

        String directMessageKey = senderId + "_" + receiverId;

        if (senderId.compareTo(receiverId) > 0) {
            directMessageKey = receiverId + "_" + senderId;
        }

        String destination = String.format("/sub/direct-messages_{%s}", directMessageKey);
        messagingTemplate.convertAndSend(destination, directMessageResponse);
    }
}
