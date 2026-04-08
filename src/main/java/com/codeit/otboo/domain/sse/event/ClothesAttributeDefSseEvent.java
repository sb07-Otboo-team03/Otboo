package com.codeit.otboo.domain.sse.event;

import java.util.UUID;

public class ClothesAttributeDefSseEvent extends BaseSseEvent {
    private UUID userId;

    public ClothesAttributeDefSseEvent(String title, String content, UUID userId) {
        super(title, content);
        this.userId = userId;
    }
}
