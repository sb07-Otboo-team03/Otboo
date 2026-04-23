package com.codeit.otboo.global.kafka;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;

public final class DirectMessageKeyGenerator {

    private DirectMessageKeyGenerator() {
    }

    public static String generate(DirectMessageResponse directMessageResponse) {
        String senderId = directMessageResponse.sender().userId().toString();
        String receiverId = directMessageResponse.receiver().userId().toString();

        return (senderId.compareTo(receiverId) < 0)
                ? senderId + "_" + receiverId
                : receiverId + "_" + senderId;
    }
}