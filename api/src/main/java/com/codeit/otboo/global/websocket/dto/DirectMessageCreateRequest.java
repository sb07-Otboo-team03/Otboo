package com.codeit.otboo.global.websocket.dto;

import java.util.UUID;

public record DirectMessageCreateRequest(
    UUID receiverId,
    UUID senderId,
    String content
) {

}
