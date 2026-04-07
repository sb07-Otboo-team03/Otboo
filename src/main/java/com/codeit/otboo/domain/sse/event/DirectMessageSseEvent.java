package com.codeit.otboo.domain.sse.event;

import java.util.UUID;

public class DirectMessageSseEvent extends BaseSseEvent {
    public DirectMessageSseEvent(String title, String content, UUID userId) {
        super(title, content, userId);
    }
}
