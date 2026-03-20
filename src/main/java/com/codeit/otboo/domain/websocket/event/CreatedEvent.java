package com.codeit.otboo.domain.websocket.event;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public abstract class CreatedEvent<T> {

    private final T data;
    private final LocalDateTime createdAt;

    protected CreatedEvent(final T data, final LocalDateTime createdAt) {
        this.data = data;
        this.createdAt = createdAt;
    }
}

