package com.codeit.otboo.global.websocket.controller;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.service.DirectMessageService;
import com.codeit.otboo.global.websocket.dto.DirectMessageCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final DirectMessageService directMessageService;

    @MessageMapping("direct-messages_send")
    public DirectMessageResponse sendMessage(@Payload DirectMessageCreateRequest request) {
        DirectMessageResponse createdMessage = directMessageService.create(request);
        log.debug("✅ 텍스트 메시지 생성 응답: {}", createdMessage);
        return createdMessage;
    }
}
