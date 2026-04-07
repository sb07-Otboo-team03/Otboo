package com.codeit.otboo.domain.sse.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClothesAttributeDefSseEvent {
    String title;
    String content;
}
