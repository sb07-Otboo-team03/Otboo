package com.codeit.otboo.domain.sse.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseSseEvent {
    private String title;
    private String content;
}

