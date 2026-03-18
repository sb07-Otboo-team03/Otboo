package com.codeit.otboo.domain.websocket.event;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import java.time.LocalDateTime;

public class DirectMessageCreatedEvent extends CreatedEvent<DirectMessageResponse> {

    public DirectMessageCreatedEvent(DirectMessageResponse data, LocalDateTime createdAt) {
        super(data, createdAt);
    }
}
