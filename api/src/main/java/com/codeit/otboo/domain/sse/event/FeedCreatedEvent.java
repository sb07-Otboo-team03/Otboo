package com.codeit.otboo.domain.sse.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class FeedCreatedEvent {
    String title;
    String content;
    List<UUID> receiverIds;
}