package com.codeit.otboo.domain.websocketNSse.event;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageCursorResponse;
import java.time.LocalDateTime;

public class DirectMessageCreatedEvent extends CreatedEvent<DirectMessageCursorResponse> {

    public DirectMessageCreatedEvent(DirectMessageCursorResponse data, LocalDateTime createdAt) {
        super(data, createdAt);
    }
}
