package com.codeit.otboo.domain.sse.event;

import java.util.UUID;
import lombok.Getter;

@Getter
public class FollowSseEvent extends BaseSseEvent {
    private UUID userId;

    public FollowSseEvent(String title, String content, UUID userId) {
        super(title, content);
        this.userId = userId;
    }
}
