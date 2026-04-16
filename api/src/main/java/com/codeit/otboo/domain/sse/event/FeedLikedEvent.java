package com.codeit.otboo.domain.sse.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public class FeedLikedEvent extends BaseSseEvent {
    private final UUID receiverId;

    public FeedLikedEvent(String title, String content, UUID receiverId) {
        super(title, content);
        this.receiverId = receiverId;
    }
}
