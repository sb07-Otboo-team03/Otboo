package com.codeit.otboo.domain.sse.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public class DirectMessageSseEvent extends BaseSseEvent {
    private final UUID userId;

    public DirectMessageSseEvent(String title, String content, UUID userId) {
        super(title, content);
        this.userId = userId;
    }
}
