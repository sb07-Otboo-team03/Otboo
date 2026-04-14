package com.codeit.otboo.domain.sse.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public class CommentCreatedEvent extends BaseSseEvent {

    private final UUID receiverId;

    public CommentCreatedEvent(String title, String content, UUID receiverId) {
        super(title, content);
        this.receiverId = receiverId;
    }
}
