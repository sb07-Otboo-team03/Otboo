package com.codeit.otboo.domain.sse.event;

import java.util.UUID;

public class ClothesAttributeDefSseEvent extends BaseSseEvent {
    public ClothesAttributeDefSseEvent(String title, String content) {
        super(title, content);
    }
}
