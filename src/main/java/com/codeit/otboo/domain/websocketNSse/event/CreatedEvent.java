package com.codeit.otboo.domain.websocketNSse.event;

import java.time.Instant;
import lombok.Getter;

@Getter
public abstract class CreatedEvent<T> {

    private final T data;
    private final Instant createdAt;

    protected CreatedEvent(final T data, final Instant createdAt) {
        this.data = data;
        this.createdAt = createdAt;
    }
}

