package com.codeit.otboo.domain.sse.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseSseEvent {
    String title;
    String content;
    UUID userId;
}

