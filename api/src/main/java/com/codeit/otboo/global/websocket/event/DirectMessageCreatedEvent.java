package com.codeit.otboo.global.websocket.event;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.global.event.CreatedEvent;
import java.time.LocalDateTime;

public class DirectMessageCreatedEvent extends CreatedEvent<DirectMessageResponse> {

    public DirectMessageCreatedEvent(DirectMessageResponse data, LocalDateTime createdAt) {
        super(data, createdAt);
    }
}
