package com.codeit.otboo.domain.websocket.dto;

import java.util.UUID;

public record DirectMessageCreateRequest(
    UUID receiverId,
    UUID senderId,
    String content
) {

}
