package com.codeit.otboo.global.event;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public abstract class DeletedEvent<T> {

    private final T data;
    private final LocalDateTime deletedAt;

    protected DeletedEvent(final T data, final LocalDateTime deletedAt) {
        this.data = data;
        this.deletedAt = deletedAt;
    }
}
