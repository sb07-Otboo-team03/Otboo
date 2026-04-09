package com.codeit.otboo.domain.sse.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserRoleUpdatedEvent extends BaseSseEvent{
    private final UUID receiverId;

    public UserRoleUpdatedEvent(String title, String content, UUID receiverId) {
        super(title, content);
        this.receiverId = receiverId;
    }
}
