package com.codeit.otboo.domain.sse.event;

import java.util.UUID;
import lombok.Getter;

@Getter
public class DirectMessageSseEvent extends BaseSseEvent {
    private UUID userId;

    public DirectMessageSseEvent(String title, String content, UUID userId) {
        super(title, content);
        this.userId = userId;
    }
}
