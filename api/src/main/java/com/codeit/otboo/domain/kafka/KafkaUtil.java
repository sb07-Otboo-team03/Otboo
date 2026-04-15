package com.codeit.otboo.domain.kafka;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;

public class KafkaUtil {

    public static String makeWebSocketKey(DirectMessageResponse directMessageResponse) {

        String senderId = directMessageResponse.sender().userId().toString();
        String receiverId = directMessageResponse.receiver().userId().toString();

        return (senderId.compareTo(receiverId) < 0) ?
            senderId + "_" + receiverId :
            receiverId + "_" + senderId;
    }
}
