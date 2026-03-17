package com.codeit.otboo.domain.websocketNSse.event;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageCursorResponse;
import java.time.Instant;

public class DirectMessageCreatedEvent extends CreatedEvent<DirectMessageCursorResponse> {

    public DirectMessageCreatedEvent(DirectMessageCursorResponse data, Instant createdAt) {
        super(data, createdAt);
    }
}
